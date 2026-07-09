import { HTMLAttributes, ReactNode } from "react";

export function Card({
  children,
  className = "",
  ...props
}: HTMLAttributes<HTMLDivElement> & { children: ReactNode }) {
  return (
    <div
      {...props}
      className={`rounded-xl border border-zinc-200/80 bg-white shadow-sm shadow-zinc-100 ${className}`}
    >
      {children}
    </div>
  );
}
