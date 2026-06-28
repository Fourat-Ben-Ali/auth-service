package com.example.demo.dto;

import com.example.demo.entity.AppUser;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String keycloakId, String username, String email,
                           String firstName, String lastName, boolean enabled,
                           Long enterpriseId, Set<RoleResponse> roles) {

    public static UserResponse from(AppUser u) {
        return new UserResponse(
                u.getId(), u.getKeycloakId(), u.getUsername(), u.getEmail(),
                u.getFirstName(), u.getLastName(), u.isEnabled(),
                u.getEnterpriseId(),
                u.getRoles().stream().map(RoleResponse::from).collect(Collectors.toSet())
        );
    }
}
