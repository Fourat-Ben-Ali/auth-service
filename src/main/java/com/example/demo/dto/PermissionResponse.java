package com.example.demo.dto;

import com.example.demo.entity.Permission;

public record PermissionResponse(Long id, String name, String description) {

    public static PermissionResponse from(Permission p) {
        return new PermissionResponse(p.getId(), p.getName(), p.getDescription());
    }
}
