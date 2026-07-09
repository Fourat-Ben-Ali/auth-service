package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        // Keycloak's default user profile requires both — a user created
        // without them can never actually log in ("Account is not fully
        // set up"), so this isn't optional in practice.
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Long enterpriseId
) {}
