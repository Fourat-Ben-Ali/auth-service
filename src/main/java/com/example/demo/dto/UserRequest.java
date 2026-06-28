package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        String firstName,
        String lastName,
        @NotNull Long enterpriseId
) {}
