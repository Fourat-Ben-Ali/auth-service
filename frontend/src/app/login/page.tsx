"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Waypoints } from "lucide-react";
import { useAuth } from "@/lib/auth";
import { Button } from "@/components/ui/Button";
import { Field, TextInput } from "@/components/ui/Field";
import { InlineError } from "@/components/ui/PageHeader";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();
  const [enterpriseSlug, setEnterpriseSlug] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await login(username, password, enterpriseSlug);
      router.push(user.isPlatformSuperAdmin || user.isSuperAdmin ? "/dashboard/users" : "/dashboard/welcome");
    } catch {
      setError("Invalid username or password");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="relative flex flex-1 items-center justify-center overflow-hidden px-4">
      <div
        className="pointer-events-none absolute inset-0 -z-10"
        style={{
          background:
            "radial-gradient(600px circle at 15% 20%, rgba(79,70,229,0.08), transparent 60%), radial-gradient(500px circle at 85% 80%, rgba(79,70,229,0.06), transparent 60%)",
        }}
      />

      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <div className="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-brand text-white shadow-lg shadow-indigo-200">
            <Waypoints size={22} />
          </div>
          <h1 className="text-lg font-semibold text-zinc-900">Workforce OS</h1>
          <p className="mt-0.5 text-sm text-zinc-500">Sign in to the admin console</p>
        </div>

        <div className="rounded-2xl border border-zinc-200/80 bg-white p-7 shadow-xl shadow-zinc-200/50">
          <form onSubmit={handleSubmit} className="space-y-4">
            <Field label="Enterprise (leave blank for platform admin)">
              <TextInput
                type="text"
                value={enterpriseSlug}
                onChange={(e) => setEnterpriseSlug(e.target.value.toLowerCase())}
                placeholder="acme-corp"
              />
            </Field>
            <Field label="Username">
              <TextInput
                type="text"
                required
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder={enterpriseSlug ? "jdoe" : "admin"}
              />
            </Field>
            <Field label="Password">
              <TextInput
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
              />
            </Field>

            {error && <InlineError>{error}</InlineError>}

            <Button type="submit" disabled={submitting} className="w-full">
              {submitting ? "Signing in..." : "Sign in"}
            </Button>
          </form>
        </div>

        <p className="mt-4 text-center text-xs text-zinc-400">
          {enterpriseSlug
            ? `Signing in to "${enterpriseSlug}"`
            : "No enterprise entered — signing in as platform super admin"}
        </p>
      </div>
    </div>
  );
}
