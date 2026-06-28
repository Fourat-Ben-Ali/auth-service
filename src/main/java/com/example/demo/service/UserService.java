package com.example.demo.service;

import com.example.demo.dto.AssignRolesRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Role;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.EnterpriseRepository;
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
public class UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Transactional
    public UserResponse create(UserRequest request) {
        if (!enterpriseRepository.existsById(request.enterpriseId())) {
            throw new NotFoundException("Enterprise not found: " + request.enterpriseId());
        }
        if (appUserRepository.existsByEmailAndEnterpriseId(request.email(), request.enterpriseId())) {
            throw new ConflictException("Email already in use in this enterprise");
        }
        if (appUserRepository.existsByUsernameAndEnterpriseId(request.username(), request.enterpriseId())) {
            throw new ConflictException("Username already in use in this enterprise");
        }

        String keycloakId = keycloakAdminService.createUser(
                request.username(), request.email(),
                request.firstName(), request.lastName(), request.password()
        );

        AppUser user = AppUser.builder()
                .keycloakId(keycloakId)
                .username(request.username())
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();
        user.setEnterpriseId(request.enterpriseId());

        return UserResponse.from(appUserRepository.save(user));
    }

    public List<UserResponse> findAll() {
        Long enterpriseId = TenantContext.getEnterpriseId();
        List<AppUser> users = enterpriseId != null
                ? appUserRepository.findAllByEnterpriseId(enterpriseId)
                : appUserRepository.findAll();
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(Long id) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        return (enterpriseId != null
                ? appUserRepository.findByIdAndEnterpriseId(id, enterpriseId)
                : appUserRepository.findById(id))
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional
    public UserResponse assignRoles(Long userId, AssignRolesRequest request) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        AppUser user = (enterpriseId != null
                ? appUserRepository.findByIdAndEnterpriseId(userId, enterpriseId)
                : appUserRepository.findById(userId))
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Set<Role> roles = request.roleIds().stream()
                .map(rid -> roleRepository.findByIdAndEnterpriseId(rid, enterpriseId)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + rid)))
                .collect(Collectors.toSet());

        user.getRoles().addAll(roles);
        return UserResponse.from(appUserRepository.save(user));
    }
}
