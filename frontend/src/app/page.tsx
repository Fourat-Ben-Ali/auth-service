"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export default function Home() {
  const { token, user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;
    if (!token) {
      router.replace("/login");
      return;
    }
    router.replace(
      user?.isPlatformSuperAdmin || user?.isSuperAdmin ? "/dashboard/users" : "/dashboard/welcome"
    );
  }, [loading, token, user, router]);

  return null;
}
