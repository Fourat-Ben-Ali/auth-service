package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignPermissionsRequest(@NotEmpty Set<Long> permissionIds) {}
