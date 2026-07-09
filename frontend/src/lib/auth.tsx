"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter } from "next/navigation";
import type { CurrentUser } from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8082";
const TOKEN_KEY = "workforce_os_token";

function decodeJwtRoles(token: string): CurrentUser | null {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    const roles: string[] = payload.realm_access?.roles ?? [];
    const issuer: string = payload.iss ?? "";
    return {
      username: payload.preferred_username ?? "",
      roles,
      isSuperAdmin: roles.includes("SUPER_ADMIN"),
      // A blank-enterprise login authenticates against Keycloak's master
      // realm — that's what makes someone a platform super admin. Mirrors
      // TenantAwareIssuerResolver/KeyckloackJwtAuthenticationConverter on
      // the backend, which is where this is actually enforced.
      isPlatformSuperAdmin: issuer.endsWith("/realms/master"),
      realmSlug: issuer.split("/realms/")[1] ?? "",
    };
  } catch {
    return null;
  }
}

interface AuthContextValue {
  token: string | null;
  user: CurrentUser | null;
  loading: boolean;
  login: (username: string, password: string, enterpriseSlug?: string) => Promise<CurrentUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const stored = localStorage.getItem(TOKEN_KEY);
    if (stored) {
      setToken(stored);
      setUser(decodeJwtRoles(stored));
    }
    setLoading(false);
  }, []);

  async function login(username: string, password: string, enterpriseSlug?: string) {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, enterpriseSlug: enterpriseSlug || undefined }),
    });
    if (!res.ok) {
      throw new Error("Invalid username or password");
    }
    const data = await res.json();
    const accessToken: string = data.access_token;
    const decoded = decodeJwtRoles(accessToken);
    if (!decoded) {
      throw new Error("Invalid token received");
    }
    localStorage.setItem(TOKEN_KEY, accessToken);
    setToken(accessToken);
    setUser(decoded);
    return decoded;
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
    router.push("/login");
  }

  return (
    <AuthContext.Provider value={{ token, user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

export { API_BASE };
