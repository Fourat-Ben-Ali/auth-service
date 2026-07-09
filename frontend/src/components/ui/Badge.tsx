import { ReactNode } from "react";

type Tone = "brand" | "success" | "neutral" | "warning" | "danger";

const tones: Record<Tone, string> = {
  brand: "bg-indigo-50 text-indigo-700 ring-1 ring-inset ring-indigo-200",
  success: "bg-emerald-50 text-emerald-700 ring-1 ring-inset ring-emerald-200",
  neutral: "bg-zinc-100 text-zinc-600 ring-1 ring-inset ring-zinc-200",
  warning: "bg-amber-50 text-amber-700 ring-1 ring-inset ring-amber-200",
  danger: "bg-rose-50 text-rose-700 ring-1 ring-inset ring-rose-200",
};

export function Badge({ tone = "neutral", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${tones[tone]}`}
    >
      {children}
    </span>
  );
}
