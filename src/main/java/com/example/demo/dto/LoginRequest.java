package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * enterpriseSlug is the Keycloak realm to authenticate against. Leave it
 * blank to log in as a platform super admin (Keycloak's master realm).
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password, String enterpriseSlug) {}
