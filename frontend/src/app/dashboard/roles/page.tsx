"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { Plus, ShieldCheck } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { Enterprise, Permission, Role } from "@/lib/types";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Field, Select, TextInput } from "@/components/ui/Field";
import { Badge } from "@/components/ui/Badge";
import { PageHeader, EmptyState, InlineError } from "@/components/ui/PageHeader";

export default function RolesPage() {
  const { user } = useAuth();
  const isPlatformAdmin = user?.isPlatformSuperAdmin ?? false;

  const [enterprises, setEnterprises] = useState<Enterprise[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ name: "", description: "", enterpriseId: "" });

  const [assigningRoleId, setAssigningRoleId] = useState<number | null>(null);
  const [assignError, setAssignError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [rl, perm] = await Promise.all([api.roles.list(), api.permissions.list()]);
      setRoles(rl);
      setPermissions(perm);
      if (isPlatformAdmin) {
        setEnterprises(await api.enterprises.list());
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Failed to load roles");
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

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setFormError(null);
    if (!form.enterpriseId) {
      setFormError("Choose an enterprise");
      return;
    }
    setSubmitting(true);
    try {
      await api.roles.create({
        name: form.name,
        description: form.description || undefined,
        enterpriseId: Number(form.enterpriseId),
      });
      setForm({ name: "", description: "", enterpriseId: "" });
      await load();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Failed to create role");
    } finally {
      setSubmitting(false);
    }
  }

  async function togglePermission(role: Role, permissionId: number, checked: boolean) {
    setAssignError(null);
    setAssigningRoleId(role.id);
    const currentIds = role.permissions.map((p) => p.id);
    const nextIds = checked
      ? Array.from(new Set([...currentIds, permissionId]))
      : currentIds.filter((id) => id !== permissionId);
    try {
      await api.roles.assignPermissions(role.id, nextIds);
      await load();
    } catch (err) {
      setAssignError(err instanceof ApiError ? err.message : "Failed to update permissions");
    } finally {
      setAssigningRoleId(null);
    }
  }

  return (
    <div>
      <PageHeader
        icon={ShieldCheck}
        title="Roles"
        description={
          isPlatformAdmin
            ? "Roles across every enterprise, each bundling permissions. Platform-owned."
            : "Roles available in your enterprise, managed by the platform admin."
        }
      />

      {isPlatformAdmin && (
        <Card className="mt-6 p-5">
          <form onSubmit={handleCreate} className="flex flex-wrap items-end gap-3">
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
            <Field label="Name" className="w-40">
              <TextInput
                type="text"
                required
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                placeholder="MANAGER"
              />
            </Field>
            <Field label="Description" className="w-56">
              <TextInput
                type="text"
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              />
            </Field>
            <Button type="submit" disabled={submitting}>
              <Plus size={16} />
              {submitting ? "Creating..." : "Add role"}
            </Button>
            {formError && <InlineError>{formError}</InlineError>}
          </form>
        </Card>
      )}

      {assignError && (
        <div className="mt-4">
          <InlineError>{assignError}</InlineError>
        </div>
      )}

      <div className="mt-4 space-y-3">
        {loading ? (
          <p className="text-sm text-zinc-400">Loading...</p>
        ) : error ? (
          <InlineError>{error}</InlineError>
        ) : roles.length === 0 ? (
          <Card>
            <EmptyState icon={ShieldCheck} message="No roles yet." />
          </Card>
        ) : (
          roles.map((role) => {
            const rolePermissionIds = new Set(role.permissions.map((p) => p.id));
            return (
              <Card key={role.id} className="p-5">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-medium text-zinc-900">{role.name}</p>
                  {isPlatformAdmin && <Badge tone="brand">{enterpriseName(role.enterpriseId)}</Badge>}
                </div>
                {role.description && (
                  <p className="mt-0.5 text-xs text-zinc-500">{role.description}</p>
                )}
                <div className="mt-4 border-t border-zinc-100 pt-3">
                  <p className="text-xs font-medium text-zinc-500">Permissions</p>
                  {isPlatformAdmin ? (
                    <div className="mt-2 flex flex-wrap gap-4">
                      {permissions.map((perm) => (
                        <label
                          key={perm.id}
                          className="flex items-center gap-1.5 text-sm text-zinc-700"
                        >
                          <input
                            type="checkbox"
                            className="h-3.5 w-3.5 rounded border-zinc-300 text-brand focus:ring-brand/30"
                            checked={rolePermissionIds.has(perm.id)}
                            disabled={assigningRoleId === role.id}
                            onChange={(e) => togglePermission(role, perm.id, e.target.checked)}
                          />
                          <span className="font-mono text-xs">{perm.name}</span>
                        </label>
                      ))}
                    </div>
                  ) : role.permissions.length === 0 ? (
                    <p className="mt-1.5 text-xs text-zinc-400">No permissions assigned.</p>
                  ) : (
                    <div className="mt-2 flex flex-wrap gap-2">
                      {role.permissions.map((perm) => (
                        <Badge key={perm.id} tone="neutral">
                          <span className="font-mono">{perm.name}</span>
                        </Badge>
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
