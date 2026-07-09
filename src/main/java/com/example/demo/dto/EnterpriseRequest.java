package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * slug doubles as the Keycloak realm name created for this enterprise.
 * The admin* fields bootstrap that realm's initial super admin account.
 */
public record EnterpriseRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "slug must be lowercase alphanumeric with dashes") String slug,
        @NotBlank String adminUsername,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8) String adminPassword,
        // Keycloak's default user profile requires both — a user created
        // without them can never actually log in ("Account is not fully
        // set up"), so this isn't optional in practice.
        @NotBlank String adminFirstName,
        @NotBlank String adminLastName
) {}
