package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PermissionRequest(
        @NotBlank @Pattern(regexp = "^[A-Z_]+$", message = "permission name must be uppercase with underscores") String name,
        String description
) {}
