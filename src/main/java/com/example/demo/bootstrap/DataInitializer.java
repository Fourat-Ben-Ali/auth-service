package com.example.demo.bootstrap;

import com.example.demo.entity.Permission;
import com.example.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Enterprises are real Keycloak realms now (see EnterpriseService) — there's
 * no "default enterprise" to seed anymore. A platform super admin creates
 * the first one through POST /api/enterprises. Only permissions (global,
 * realm-agnostic) are seeded at startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;

    // Roles/permissions themselves are platform-owned (see RoleController /
    // PermissionController — creation is hasRole('PLATFORM_SUPER_ADMIN')
    // only, no authority bypass). What's delegatable is account management
    // and read-only visibility into what roles/permissions exist.
    private static final List<String[]> PERMISSIONS = List.of(
            new String[]{"USER_CREATE",       "Create users"},
            new String[]{"USER_READ",         "Read users"},
            new String[]{"USER_UPDATE",       "Modify and enable/disable users"},
            new String[]{"USER_ASSIGN_ROLE",  "Assign roles to users"},
            new String[]{"ROLE_READ",         "Read roles"},
            new String[]{"PERMISSION_READ",   "Read permissions"}
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String[] p : PERMISSIONS) {
            if (!permissionRepository.existsByName(p[0])) {
                permissionRepository.save(Permission.builder().name(p[0]).description(p[1]).build());
                log.info("Seeded permission: {}", p[0]);
            }
        }
    }
}
