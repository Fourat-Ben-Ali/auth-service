"use client";

import { Sparkles } from "lucide-react";
import { useAuth } from "@/lib/auth";

function titleCase(slug: string) {
  return slug
    .split("-")
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

export default function WelcomePage() {
  const { user } = useAuth();

  return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-50 text-brand">
        <Sparkles size={26} />
      </div>
      <h1 className="text-xl font-semibold text-zinc-900">
        Welcome to {titleCase(user?.realmSlug ?? "")}
      </h1>
      <p className="mt-1 text-sm text-zinc-500">
        Signed in as <span className="font-medium text-zinc-700">{user?.username}</span>
      </p>
    </div>
  );
}
