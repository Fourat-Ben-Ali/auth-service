export interface Enterprise {
  id: number;
  name: string;
  slug: string;
  enabled: boolean;
  createdAt: string;
}

export interface Permission {
  id: number;
  name: string;
  description: string | null;
}

export interface Role {
  id: number;
  name: string;
  description: string | null;
  enterpriseId: number;
  permissions: Permission[];
}

export interface AppUser {
  id: number;
  keycloakId: string;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  enabled: boolean;
  enterpriseId: number;
  roles: Role[];
}

export interface CurrentUser {
  username: string;
  roles: string[];
  isSuperAdmin: boolean;
  isPlatformSuperAdmin: boolean;
  /** Keycloak realm slug this session was authenticated against ("master" for platform admins). */
  realmSlug: string;
}
