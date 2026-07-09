package com.example.demo.service;

import com.example.demo.dto.AssignRolesRequest;
import com.example.demo.dto.SetEnabledRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Enterprise;
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
        Enterprise enterprise = enterpriseRepository.findById(request.enterpriseId())
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + request.enterpriseId()));
        if (appUserRepository.existsByEmailAndEnterpriseId(request.email(), request.enterpriseId())) {
            throw new ConflictException("Email already in use in this enterprise");
        }
        if (appUserRepository.existsByUsernameAndEnterpriseId(request.username(), request.enterpriseId())) {
            throw new ConflictException("Username already in use in this enterprise");
        }

        String keycloakId = keycloakAdminService.createUser(
                enterprise.getSlug(), request.username(), request.email(),
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
        return UserResponse.from(findScoped(id));
    }

    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        AppUser user = findScoped(userId);
        if (!user.getEmail().equals(request.email())
                && appUserRepository.existsByEmailAndEnterpriseId(request.email(), user.getEnterpriseId())) {
            throw new ConflictException("Email already in use in this enterprise");
        }
        Enterprise enterprise = enterpriseRepository.findById(user.getEnterpriseId())
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + user.getEnterpriseId()));

        keycloakAdminService.updateProfile(
                enterprise.getSlug(), user.getKeycloakId(), request.email(), request.firstName(), request.lastName());

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        return UserResponse.from(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse setEnabled(Long userId, SetEnabledRequest request) {
        AppUser user = findScoped(userId);
        Enterprise enterprise = enterpriseRepository.findById(user.getEnterpriseId())
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + user.getEnterpriseId()));

        keycloakAdminService.setEnabled(enterprise.getSlug(), user.getKeycloakId(), request.enabled());

        user.setEnabled(request.enabled());
        return UserResponse.from(appUserRepository.save(user));
    }

    private AppUser findScoped(Long userId) {
        Long enterpriseId = TenantContext.getEnterpriseId();
        return (enterpriseId != null
                ? appUserRepository.findByIdAndEnterpriseId(userId, enterpriseId)
                : appUserRepository.findById(userId))
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserResponse assignRoles(Long userId, AssignRolesRequest request) {
        AppUser user = findScoped(userId);

        Set<Role> roles = request.roleIds().stream()
                .map(rid -> roleRepository.findByIdAndEnterpriseId(rid, user.getEnterpriseId())
                        .orElseThrow(() -> new NotFoundException("Role not found: " + rid)))
                .collect(Collectors.toSet());

        user.getRoles().addAll(roles);
        return UserResponse.from(appUserRepository.save(user));
    }
}
