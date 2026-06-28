package com.example.demo.repository;

import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByEnterpriseId(Long enterpriseId);
    Optional<Role> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    boolean existsByNameAndEnterpriseId(String name, Long enterpriseId);
}
