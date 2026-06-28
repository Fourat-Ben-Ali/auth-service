package com.example.demo.dto;

import com.example.demo.entity.Enterprise;

import java.time.LocalDateTime;

public record EnterpriseResponse(Long id, String name, String slug, boolean enabled, LocalDateTime createdAt) {

    public static EnterpriseResponse from(Enterprise e) {
        return new EnterpriseResponse(e.getId(), e.getName(), e.getSlug(), e.isEnabled(), e.getCreatedAt());
    }
}
