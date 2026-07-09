"use client";

import { ReactNode, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Building2,
  KeyRound,
  ListChecks,
  LogOut,
  Sparkles,
  ShieldCheck,
  Users2,
  Waypoints,
} from "lucide-react";
import { useAuth } from "@/lib/auth";

const PLATFORM_NAV_ITEMS = [
  { href: "/dashboard/enterprises", label: "Enterprises", icon: Building2 },
  { href: "/dashboard/users", label: "Users", icon: Users2 },
  { href: "/dashboard/roles", label: "Roles", icon: ShieldCheck },
  { href: "/dashboard/permissions", label: "Permissions", icon: KeyRound },
];

const ENTERPRISE_ADMIN_NAV_ITEMS = [
  { href: "/dashboard/users", label: "Users", icon: Users2 },
  { href: "/dashboard/roles", label: "Roles", icon: ShieldCheck },
];

const REGULAR_NAV_ITEMS = [
  { href: "/dashboard/welcome", label: "Welcome", icon: Sparkles },
  { href: "/dashboard/tasks", label: "Tasks", icon: ListChecks },
];

function initials(name: string) {
  return name.slice(0, 2).toUpperCase();
}

export default function DashboardLayout({ children }: { children: ReactNode }) {
  const { token, user, loading, logout } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !token) {
      router.replace("/login");
    }
  }, [loading, token, router]);

  if (loading || !token) {
    return (
      <div className="flex flex-1 items-center justify-center text-sm text-zinc-400">
        Loading...
      </div>
    );
  }

  const navItems = user?.isPlatformSuperAdmin
    ? PLATFORM_NAV_ITEMS
    : user?.isSuperAdmin
      ? ENTERPRISE_ADMIN_NAV_ITEMS
      : REGULAR_NAV_ITEMS;

  return (
    <div className="flex flex-1">
      <aside className="flex w-64 shrink-0 flex-col border-r border-zinc-200 bg-white">
        <div className="flex items-center gap-2 px-5 py-5">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white">
            <Waypoints size={17} />
          </div>
          <div>
            <p className="text-sm font-semibold leading-tight text-zinc-900">Workforce OS</p>
            <p className="text-[11px] leading-tight text-zinc-400">Admin console</p>
          </div>
        </div>

        <nav className="flex flex-1 flex-col gap-0.5 px-3">
          {navItems.map((item) => {
            const active = pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`group relative flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  active
                    ? "bg-indigo-50 text-brand"
                    : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900"
                }`}
              >
                {active && (
                  <span className="absolute left-0 top-1/2 h-4 w-0.5 -translate-y-1/2 rounded-r bg-brand" />
                )}
                <Icon size={17} strokeWidth={2} />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t border-zinc-100 p-3">
          <div className="flex items-center gap-2.5 rounded-lg px-2 py-2">
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-zinc-900 text-xs font-semibold text-white">
              {initials(user?.username ?? "?")}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-zinc-900">{user?.username}</p>
              {user?.isPlatformSuperAdmin ? (
                <span className="inline-block rounded bg-amber-100 px-1.5 py-0.5 text-[10px] font-semibold text-amber-800">
                  PLATFORM ADMIN
                </span>
              ) : user?.isSuperAdmin ? (
                <span className="inline-block rounded bg-indigo-100 px-1.5 py-0.5 text-[10px] font-semibold text-indigo-800">
                  ENTERPRISE ADMIN
                </span>
              ) : null}
            </div>
            <button
              onClick={logout}
              title="Sign out"
              className="flex h-8 w-8 items-center justify-center rounded-lg text-zinc-400 hover:bg-rose-50 hover:text-rose-600"
            >
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </aside>
      <main className="flex-1 overflow-auto bg-zinc-50 p-8">
        <div className="mx-auto max-w-5xl space-y-6">{children}</div>
      </main>
    </div>
  );
}
