package com.example.demo.repository;

import com.example.demo.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByKeycloakId(String keycloakId);
    Optional<AppUser> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    List<AppUser> findAllByEnterpriseId(Long enterpriseId);
    boolean existsByEmailAndEnterpriseId(String email, Long enterpriseId);
    boolean existsByUsernameAndEnterpriseId(String username, Long enterpriseId);
}
