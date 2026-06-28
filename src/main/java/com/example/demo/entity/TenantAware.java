package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantAware {

    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId;
}
