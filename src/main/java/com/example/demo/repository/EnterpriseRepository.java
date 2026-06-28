package com.example.demo.repository;

import com.example.demo.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Optional<Enterprise> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
