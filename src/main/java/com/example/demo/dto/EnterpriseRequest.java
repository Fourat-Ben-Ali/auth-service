package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnterpriseRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "slug must be lowercase alphanumeric with dashes") String slug
) {}
