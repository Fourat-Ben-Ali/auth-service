import { ReactNode } from "react";
import { LucideIcon } from "lucide-react";

export function PageHeader({
  icon: Icon,
  title,
  description,
}: {
  icon: LucideIcon;
  title: string;
  description: string;
}) {
  return (
    <div className="flex items-center gap-3">
      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50 text-brand">
        <Icon size={20} strokeWidth={2} />
      </div>
      <div>
        <h1 className="text-lg font-semibold tracking-tight text-zinc-900">{title}</h1>
        <p className="text-sm text-zinc-500">{description}</p>
      </div>
    </div>
  );
}

export function EmptyState({ icon: Icon, message }: { icon: LucideIcon; message: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 py-12 text-center">
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-zinc-100 text-zinc-400">
        <Icon size={18} />
      </div>
      <p className="text-sm text-zinc-500">{message}</p>
    </div>
  );
}

export function InlineError({ children }: { children: ReactNode }) {
  return (
    <p className="w-full rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{children}</p>
  );
}
