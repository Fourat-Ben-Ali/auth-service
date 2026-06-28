package com.example.demo.bootstrap;

import com.example.demo.entity.Enterprise;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.repository.EnterpriseRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final RoleRepository roleRepository;

    private static final List<String[]> PERMISSIONS = List.of(
            new String[]{"USER_CREATE",       "Create users"},
            new String[]{"USER_READ",         "Read users"},
            new String[]{"USER_ASSIGN_ROLE",  "Assign roles to users"},
            new String[]{"ROLE_CREATE",       "Create roles"},
            new String[]{"ROLE_READ",         "Read roles"},
            new String[]{"ROLE_ASSIGN_PERM",  "Assign permissions to roles"},
            new String[]{"PERMISSION_CREATE", "Create permissions"},
            new String[]{"PERMISSION_READ",   "Read permissions"}
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPermissions();
        seedDefaultEnterprise();
    }

    private void seedPermissions() {
        for (String[] p : PERMISSIONS) {
            if (!permissionRepository.existsByName(p[0])) {
                permissionRepository.save(Permission.builder().name(p[0]).description(p[1]).build());
                log.info("Seeded permission: {}", p[0]);
            }
        }
    }

    private void seedDefaultEnterprise() {
        if (enterpriseRepository.existsBySlug("default")) {
            return;
        }
        Enterprise enterprise = enterpriseRepository.save(
                Enterprise.builder().name("Default Enterprise").slug("default").build()
        );
        log.info("Seeded default enterprise (id={})", enterprise.getId());

        Set<Permission> allPermissions = PERMISSIONS.stream()
                .map(p -> permissionRepository.findByName(p[0]).orElseThrow())
                .collect(Collectors.toSet());

        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Full access role")
                .permissions(allPermissions)
                .build();
        adminRole.setEnterpriseId(enterprise.getId());
        roleRepository.save(adminRole);
        log.info("Seeded ADMIN role for default enterprise");
    }
}
