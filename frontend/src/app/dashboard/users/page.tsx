"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { Pencil, Plus, Power, Users2, X } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { AppUser, Enterprise, Role } from "@/lib/types";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Field, Select, TextInput } from "@/components/ui/Field";
import { Badge } from "@/components/ui/Badge";
import { PageHeader, EmptyState, InlineError } from "@/components/ui/PageHeader";

function initials(name: string) {
  return name.slice(0, 2).toUpperCase();
}

export default function UsersPage() {
  const { user } = useAuth();
  const isPlatformAdmin = user?.isPlatformSuperAdmin ?? false;

  const [enterprises, setEnterprises] = useState<Enterprise[]>([]);
  const [users, setUsers] = useState<AppUser[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedEnterpriseId, setSelectedEnterpriseId] = useState<number | "all">("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    enterpriseId: "",
  });

  const [assigningUserId, setAssigningUserId] = useState<number | null>(null);
  const [assignError, setAssignError] = useState<string | null>(null);

  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({ email: "", firstName: "", lastName: "" });
  const [editError, setEditError] = useState<string | null>(null);
  const [savingEdit, setSavingEdit] = useState(false);
  const [togglingUserId, setTogglingUserId] = useState<number | null>(null);

  // Enterprise super admins can only see their own enterprise's users, so
  // there's nothing to pick — infer it from their own row instead of
  // calling GET /api/enterprises (platform-admin only).
  const ownEnterpriseId = useMemo(
    () => users.find((u) => u.username === user?.username)?.enterpriseId,
    [users, user?.username]
  );

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [usr, rl] = await Promise.all([api.users.list(), api.roles.list()]);
      setUsers(usr);
      setRoles(rl);
      if (isPlatformAdmin) {
        setEnterprises(await api.enterprises.list());
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Failed to load users");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPlatformAdmin]);

  const enterpriseName = useMemo(() => {
    const map = new Map(enterprises.map((e) => [e.id, e.name]));
    return (id: number) => map.get(id) ?? `#${id}`;
  }, [enterprises]);

  const visibleUsers = useMemo(
    () =>
      selectedEnterpriseId === "all"
        ? users
        : users.filter((u) => u.enterpriseId === selectedEnterpriseId),
    [users, selectedEnterpriseId]
  );

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setFormError(null);
    const enterpriseId = isPlatformAdmin ? Number(form.enterpriseId) : ownEnterpriseId;
    if (!enterpriseId) {
      setFormError(isPlatformAdmin ? "Choose an enterprise" : "Could not determine your enterprise");
      return;
    }
    setSubmitting(true);
    try {
      await api.users.create({
        username: form.username,
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        enterpriseId,
      });
      setForm({ username: "", email: "", password: "", firstName: "", lastName: "", enterpriseId: "" });
      await load();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Failed to create user");
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleRole(user: AppUser, roleId: number, checked: boolean) {
    setAssignError(null);
    setAssigningUserId(user.id);
    const currentRoleIds = user.roles.map((r) => r.id);
    const nextRoleIds = checked
      ? Array.from(new Set([...currentRoleIds, roleId]))
      : currentRoleIds.filter((id) => id !== roleId);
    try {
      await api.users.assignRoles(user.id, nextRoleIds);
      await load();
    } catch (err) {
      setAssignError(err instanceof ApiError ? err.message : "Failed to update roles");
    } finally {
      setAssigningUserId(null);
    }
  }

  function startEdit(u: AppUser) {
    setEditingUserId(u.id);
    setEditError(null);
    setEditForm({ email: u.email, firstName: u.firstName ?? "", lastName: u.lastName ?? "" });
  }

  async function saveEdit(userId: number) {
    setEditError(null);
    setSavingEdit(true);
    try {
      await api.users.update(userId, editForm);
      setEditingUserId(null);
      await load();
    } catch (err) {
      setEditError(err instanceof ApiError ? err.message : "Failed to update user");
    } finally {
      setSavingEdit(false);
    }
  }

  async function toggleEnabled(u: AppUser) {
    setTogglingUserId(u.id);
    try {
      await api.users.setEnabled(u.id, !u.enabled);
      await load();
    } catch (err) {
      setAssignError(err instanceof ApiError ? err.message : "Failed to update account status");
    } finally {
      setTogglingUserId(null);
    }
  }

  return (
    <div>
      <PageHeader
        icon={Users2}
        title="Users"
        description={
          isPlatformAdmin
            ? "Users across every enterprise on the platform."
            : "Users in your enterprise."
        }
      />

      <Card className="mt-6 p-5">
        <form onSubmit={handleCreate} className="flex flex-wrap items-end gap-3">
          {isPlatformAdmin && (
            <Field label="Enterprise" className="w-40">
              <Select
                required
                value={form.enterpriseId}
                onChange={(e) => setForm((f) => ({ ...f, enterpriseId: e.target.value }))}
              >
                <option value="">Select...</option>
                {enterprises.map((ent) => (
                  <option key={ent.id} value={ent.id}>
                    {ent.name}
                  </option>
                ))}
              </Select>
            </Field>
          )}
          <Field label="Username" className="w-36">
            <TextInput
              type="text"
              required
              value={form.username}
              onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
            />
          </Field>
          <Field label="Email" className="w-52">
            <TextInput
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            />
          </Field>
          <Field label="Password" className="w-36">
            <TextInput
              type="password"
              required
              minLength={8}
              value={form.password}
              onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
            />
          </Field>
          <Field label="First name" className="w-32">
            <TextInput
              type="text"
              required
              value={form.firstName}
              onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
            />
          </Field>
          <Field label="Last name" className="w-32">
            <TextInput
              type="text"
              required
              value={form.lastName}
              onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
            />
          </Field>
          <Button type="submit" disabled={submitting}>
            <Plus size={16} />
            {submitting ? "Creating..." : "Add user"}
          </Button>
          {formError && <InlineError>{formError}</InlineError>}
        </form>
      </Card>

      {isPlatformAdmin && (
        <div className="mt-6 flex items-center gap-2">
          <label className="text-xs font-medium text-zinc-500">Filter by enterprise</label>
          <Select
            value={selectedEnterpriseId}
            onChange={(e) =>
              setSelectedEnterpriseId(e.target.value === "all" ? "all" : Number(e.target.value))
            }
            className="w-48"
          >
            <option value="all">All enterprises</option>
            {enterprises.map((ent) => (
              <option key={ent.id} value={ent.id}>
                {ent.name}
              </option>
            ))}
          </Select>
        </div>
      )}

      {assignError && (
        <div className="mt-3">
          <InlineError>{assignError}</InlineError>
        </div>
      )}

      <div className="mt-3 space-y-3">
        {loading ? (
          <p className="text-sm text-zinc-400">Loading...</p>
        ) : error ? (
          <InlineError>{error}</InlineError>
        ) : visibleUsers.length === 0 ? (
          <Card>
            <EmptyState icon={Users2} message="No users found for this filter." />
          </Card>
        ) : (
          visibleUsers.map((u) => {
            const enterpriseRoles = roles.filter((r) => r.enterpriseId === u.enterpriseId);
            const userRoleIds = new Set(u.roles.map((r) => r.id));
            const isEditing = editingUserId === u.id;
            return (
              <Card key={u.id} className="p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-indigo-100 text-xs font-semibold text-brand">
                      {initials(u.username)}
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-zinc-900">
                        {u.username}{" "}
                        <span className="font-normal text-zinc-400">&lt;{u.email}&gt;</span>
                      </p>
                      <div className="mt-0.5 flex items-center gap-2">
                        {isPlatformAdmin && <Badge tone="brand">{enterpriseName(u.enterpriseId)}</Badge>}
                        <Badge tone={u.enabled ? "success" : "neutral"}>
                          {u.enabled ? "Enabled" : "Disabled"}
                        </Badge>
                      </div>
                    </div>
                  </div>
                  <div className="flex shrink-0 items-center gap-1">
                    <button
                      onClick={() => (isEditing ? setEditingUserId(null) : startEdit(u))}
                      title={isEditing ? "Cancel" : "Edit"}
                      className="flex h-8 w-8 items-center justify-center rounded-lg text-zinc-400 hover:bg-zinc-100 hover:text-zinc-700"
                    >
                      {isEditing ? <X size={15} /> : <Pencil size={15} />}
                    </button>
                    <button
                      onClick={() => toggleEnabled(u)}
                      disabled={togglingUserId === u.id}
                      title={u.enabled ? "Disable account" : "Enable account"}
                      className={`flex h-8 w-8 items-center justify-center rounded-lg disabled:opacity-50 ${
                        u.enabled
                          ? "text-zinc-400 hover:bg-rose-50 hover:text-rose-600"
                          : "text-zinc-400 hover:bg-emerald-50 hover:text-emerald-600"
                      }`}
                    >
                      <Power size={15} />
                    </button>
                  </div>
                </div>

                {isEditing && (
                  <div className="mt-3 flex flex-wrap items-end gap-3 border-t border-zinc-100 pt-3">
                    <Field label="Email" className="w-52">
                      <TextInput
                        type="email"
                        value={editForm.email}
                        onChange={(e) => setEditForm((f) => ({ ...f, email: e.target.value }))}
                      />
                    </Field>
                    <Field label="First name" className="w-32">
                      <TextInput
                        type="text"
                        value={editForm.firstName}
                        onChange={(e) => setEditForm((f) => ({ ...f, firstName: e.target.value }))}
                      />
                    </Field>
                    <Field label="Last name" className="w-32">
                      <TextInput
                        type="text"
                        value={editForm.lastName}
                        onChange={(e) => setEditForm((f) => ({ ...f, lastName: e.target.value }))}
                      />
                    </Field>
                    <Button type="button" disabled={savingEdit} onClick={() => saveEdit(u.id)}>
                      {savingEdit ? "Saving..." : "Save"}
                    </Button>
                    {editError && <InlineError>{editError}</InlineError>}
                  </div>
                )}

                <div className="mt-4 border-t border-zinc-100 pt-3">
                  <p className="text-xs font-medium text-zinc-500">Roles</p>
                  {enterpriseRoles.length === 0 ? (
                    <p className="mt-1.5 text-xs text-zinc-400">
                      No roles defined for this enterprise yet.
                    </p>
                  ) : (
                    <div className="mt-2 flex flex-wrap gap-4">
                      {enterpriseRoles.map((role) => (
                        <label
                          key={role.id}
                          className="flex items-center gap-1.5 text-sm text-zinc-700"
                        >
                          <input
                            type="checkbox"
                            className="h-3.5 w-3.5 rounded border-zinc-300 text-brand focus:ring-brand/30"
                            checked={userRoleIds.has(role.id)}
                            disabled={assigningUserId === u.id}
                            onChange={(e) => toggleRole(u, role.id, e.target.checked)}
                          />
                          {role.name}
                        </label>
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            );
          })
        )}
      </div>
    </div>
  );
}
