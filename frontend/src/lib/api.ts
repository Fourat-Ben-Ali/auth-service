import { API_BASE } from "./auth";
import type { AppUser, Enterprise, Permission, Role } from "./types";

const TOKEN_KEY = "workforce_os_token";

class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem(TOKEN_KEY) : null;
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (res.status === 401 || res.status === 403) {
    throw new ApiError(res.status, "You are not authorized to perform this action");
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: res.statusText }));
    throw new ApiError(res.status, body.error ?? "Request failed");
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  enterprises: {
    list: () => request<Enterprise[]>("/api/enterprises"),
    create: (data: {
      name: string;
      slug: string;
      adminUsername: string;
      adminEmail: string;
      adminPassword: string;
      adminFirstName: string;
      adminLastName: string;
    }) => request<Enterprise>("/api/enterprises", { method: "POST", body: JSON.stringify(data) }),
  },
  users: {
    list: () => request<AppUser[]>("/api/users"),
    create: (data: {
      username: string;
      email: string;
      password: string;
      firstName: string;
      lastName: string;
      enterpriseId: number;
    }) => request<AppUser>("/api/users", { method: "POST", body: JSON.stringify(data) }),
    update: (userId: number, data: { email: string; firstName: string; lastName: string }) =>
      request<AppUser>(`/api/users/${userId}`, { method: "PUT", body: JSON.stringify(data) }),
    setEnabled: (userId: number, enabled: boolean) =>
      request<AppUser>(`/api/users/${userId}/enabled`, {
        method: "PATCH",
        body: JSON.stringify({ enabled }),
      }),
    assignRoles: (userId: number, roleIds: number[]) =>
      request<AppUser>(`/api/users/${userId}/roles`, {
        method: "POST",
        body: JSON.stringify({ roleIds }),
      }),
  },
  roles: {
    list: () => request<Role[]>("/api/roles"),
    create: (data: { name: string; description?: string; enterpriseId?: number }) =>
      request<Role>("/api/roles", { method: "POST", body: JSON.stringify(data) }),
    assignPermissions: (roleId: number, permissionIds: number[]) =>
      request<Role>(`/api/roles/${roleId}/permissions`, {
        method: "POST",
        body: JSON.stringify({ permissionIds }),
      }),
  },
  permissions: {
    list: () => request<Permission[]>("/api/permissions"),
    create: (data: { name: string; description?: string }) =>
      request<Permission>("/api/permissions", { method: "POST", body: JSON.stringify(data) }),
  },
};

export { ApiError };
