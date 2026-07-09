"use client";

import { FormEvent, useEffect, useState } from "react";
import { Building2, Plus } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { Enterprise } from "@/lib/types";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Field, TextInput } from "@/components/ui/Field";
import { Badge } from "@/components/ui/Badge";
import { PageHeader, EmptyState, InlineError } from "@/components/ui/PageHeader";

const emptyForm = {
  name: "",
  slug: "",
  adminUsername: "",
  adminEmail: "",
  adminPassword: "",
  adminFirstName: "",
  adminLastName: "",
};

export default function EnterprisesPage() {
  const [enterprises, setEnterprises] = useState<Enterprise[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  function set<K extends keyof typeof emptyForm>(key: K, value: string) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setEnterprises(await api.enterprises.list());
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Failed to load enterprises");
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
      await api.enterprises.create({
        name: form.name,
        slug: form.slug,
        adminUsername: form.adminUsername,
        adminEmail: form.adminEmail,
        adminPassword: form.adminPassword,
        adminFirstName: form.adminFirstName,
        adminLastName: form.adminLastName,
      });
      setForm(emptyForm);
      await load();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Failed to create enterprise");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <PageHeader
        icon={Building2}
        title="Enterprises"
        description="Each entreprise provisions its own real Keycloak realm — platform super admin only."
      />

      <Card className="mt-6 p-5">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">
              Entreprise
            </p>
            <div className="flex flex-wrap items-end gap-3">
              <Field label="Name" className="w-48">
                <TextInput
                  required
                  value={form.name}
                  onChange={(e) => set("name", e.target.value)}
                  placeholder="Acme Corp"
                />
              </Field>
              <Field label="Slug (realm name)" className="w-48">
                <TextInput
                  required
                  value={form.slug}
                  onChange={(e) => set("slug", e.target.value.toLowerCase())}
                  placeholder="acme-corp"
                  pattern="^[a-z0-9\-]+$"
                />
              </Field>
            </div>
          </div>

          <div>
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">
              Initial (realm) super admin
            </p>
            <div className="flex flex-wrap items-end gap-3">
              <Field label="Username" className="w-36">
                <TextInput
                  required
                  value={form.adminUsername}
                  onChange={(e) => set("adminUsername", e.target.value)}
                />
              </Field>
              <Field label="Email" className="w-52">
                <TextInput
                  type="email"
                  required
                  value={form.adminEmail}
                  onChange={(e) => set("adminEmail", e.target.value)}
                />
              </Field>
              <Field label="Password" className="w-36">
                <TextInput
                  type="password"
                  required
                  minLength={8}
                  value={form.adminPassword}
                  onChange={(e) => set("adminPassword", e.target.value)}
                />
              </Field>
              <Field label="First name" className="w-32">
                <TextInput
                  required
                  value={form.adminFirstName}
                  onChange={(e) => set("adminFirstName", e.target.value)}
                />
              </Field>
              <Field label="Last name" className="w-32">
                <TextInput
                  required
                  value={form.adminLastName}
                  onChange={(e) => set("adminLastName", e.target.value)}
                />
              </Field>
              <Button type="submit" disabled={submitting}>
                <Plus size={16} />
                {submitting ? "Creating..." : "Add enterprise"}
              </Button>
            </div>
          </div>

          {formError && <InlineError>{formError}</InlineError>}
        </form>
      </Card>

      <Card className="mt-6 overflow-hidden">
        {loading ? (
          <p className="p-6 text-sm text-zinc-400">Loading...</p>
        ) : error ? (
          <div className="p-4">
            <InlineError>{error}</InlineError>
          </div>
        ) : enterprises.length === 0 ? (
          <EmptyState icon={Building2} message="No enterprises yet — add one above." />
        ) : (
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-400">
              <tr>
                <th className="px-5 py-3 font-medium">Name</th>
                <th className="px-5 py-3 font-medium">Realm (slug)</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium">Created</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {enterprises.map((ent) => (
                <tr key={ent.id} className="hover:bg-zinc-50/60">
                  <td className="px-5 py-3 font-medium text-zinc-900">{ent.name}</td>
                  <td className="px-5 py-3 font-mono text-xs text-zinc-500">{ent.slug}</td>
                  <td className="px-5 py-3">
                    <Badge tone={ent.enabled ? "success" : "neutral"}>
                      {ent.enabled ? "Enabled" : "Disabled"}
                    </Badge>
                  </td>
                  <td className="px-5 py-3 text-zinc-400">
                    {new Date(ent.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
