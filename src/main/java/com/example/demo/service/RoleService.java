package com.example.demo.service;

import com.example.demo.dto.AssignPermissionsRequest;
import com.example.demo.dto.RoleRequest;
import com.example.demo.dto.RoleResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.EnterpriseRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EnterpriseRepository enterpriseRepository;

    public RoleResponse create(RoleRequest request) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        if (enterpriseId == null) {
            enterpriseId = request.enterpriseId();
        }
        if (enterpriseId == null) {
            throw new IllegalArgumentException("enterpriseId is required");
        }
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new NotFoundException("Enterprise not found: " + enterpriseId);
        }
        if (roleRepository.existsByNameAndEnterpriseId(request.name(), enterpriseId)) {
            throw new ConflictException("Role '" + request.name() + "' already exists in this enterprise");
        }
        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .build();
        role.setEnterpriseId(enterpriseId);
        return RoleResponse.from(roleRepository.save(role));
    }

    public List<RoleResponse> findAll() {
        Long enterpriseId = TenantContext.getEnterpriseId();
        List<Role> roles = enterpriseId != null
                ? roleRepository.findAllByEnterpriseId(enterpriseId)
                : roleRepository.findAll();
        return roles.stream().map(RoleResponse::from).toList();
    }

    public RoleResponse findById(Long id) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        return (enterpriseId != null
                ? roleRepository.findByIdAndEnterpriseId(id, enterpriseId)
                : roleRepository.findById(id))
                .map(RoleResponse::from)
                .orElseThrow(() -> new NotFoundException("Role not found: " + id));
    }

    @Transactional
    public RoleResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        Role role = (enterpriseId != null
                ? roleRepository.findByIdAndEnterpriseId(roleId, enterpriseId)
                : roleRepository.findById(roleId))
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));

        Set<Permission> permissions = request.permissionIds().stream()
                .map(pid -> permissionRepository.findById(pid)
                        .orElseThrow(() -> new NotFoundException("Permission not found: " + pid)))
                .collect(Collectors.toSet());

        role.getPermissions().addAll(permissions);
        return RoleResponse.from(roleRepository.save(role));
    }
}
