"use client";

import { FormEvent, useEffect, useState } from "react";
import { KeyRound, Plus } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { Permission } from "@/lib/types";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Field, TextInput } from "@/components/ui/Field";
import { PageHeader, EmptyState, InlineError } from "@/components/ui/PageHeader";

export default function PermissionsPage() {
  const { user } = useAuth();
  const isPlatformAdmin = user?.isPlatformSuperAdmin ?? false;

  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setPermissions(await api.permissions.list());
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Failed to load permissions");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setFormError(null);
    setSubmitting(true);
    try {
      await api.permissions.create({ name: name.toUpperCase(), description: description || undefined });
      setName("");
      setDescription("");
      await load();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Failed to create permission");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <PageHeader
        icon={KeyRound}
        title="Permissions"
        description="Global permissions available to attach to roles across all enterprises."
      />

      {isPlatformAdmin && (
        <Card className="mt-6 p-5">
          <form onSubmit={handleCreate} className="flex flex-wrap items-end gap-3">
            <Field label="Name" className="w-56">
              <TextInput
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="REPORT_EXPORT"
                pattern="^[A-Za-z_]+$"
                className="font-mono"
              />
            </Field>
            <Field label="Description" className="w-64">
              <TextInput
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </Field>
            <Button type="submit" disabled={submitting}>
              <Plus size={16} />
              {submitting ? "Creating..." : "Add permission"}
            </Button>
            {formError && <InlineError>{formError}</InlineError>}
          </form>
        </Card>
      )}

      <Card className="mt-6 overflow-hidden">
        {loading ? (
          <p className="p-6 text-sm text-zinc-400">Loading...</p>
        ) : error ? (
          <div className="p-4">
            <InlineError>{error}</InlineError>
          </div>
        ) : permissions.length === 0 ? (
          <EmptyState icon={KeyRound} message="No permissions yet — add one above." />
        ) : (
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-400">
              <tr>
                <th className="px-5 py-3 font-medium">Name</th>
                <th className="px-5 py-3 font-medium">Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {permissions.map((p) => (
                <tr key={p.id} className="hover:bg-zinc-50/60">
                  <td className="px-5 py-3 font-mono text-xs text-zinc-900">{p.name}</td>
                  <td className="px-5 py-3 text-zinc-500">{p.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
