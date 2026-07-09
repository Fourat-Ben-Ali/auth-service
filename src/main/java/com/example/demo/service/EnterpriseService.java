package com.example.demo.service;

import com.example.demo.dto.EnterpriseRequest;
import com.example.demo.dto.EnterpriseResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.Enterprise;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private final EnterpriseRepository enterpriseRepository;
    private final AppUserRepository appUserRepository;
    private final KeycloakRealmAdminService keycloakRealmAdminService;

    /**
     * Provisions a brand new Keycloak realm for this enterprise — a
     * direct-access-grants client, a SUPER_ADMIN realm role, and the
     * enterprise's first (super admin) user — then mirrors it locally.
     * Only a platform super admin (master realm) can reach this.
     */
    @Transactional
    public EnterpriseResponse create(EnterpriseRequest request) {
        if (enterpriseRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Enterprise with slug '" + request.slug() + "' already exists");
        }
        if (keycloakRealmAdminService.realmExists(request.slug())) {
            throw new ConflictException("A Keycloak realm named '" + request.slug() + "' already exists");
        }

        keycloakRealmAdminService.createRealm(request.slug(), request.name());
        keycloakRealmAdminService.createDirectAccessClient(request.slug());
        keycloakRealmAdminService.createRealmRole(
                request.slug(), SUPER_ADMIN_ROLE, "Enterprise super administrator");
        String keycloakId = keycloakRealmAdminService.createUserWithRealmRole(
                request.slug(), request.adminUsername(), request.adminEmail(),
                request.adminFirstName(), request.adminLastName(), request.adminPassword(),
                SUPER_ADMIN_ROLE);

        Enterprise enterprise = enterpriseRepository.save(
                Enterprise.builder().name(request.name()).slug(request.slug()).build());

        AppUser admin = AppUser.builder()
                .keycloakId(keycloakId)
                .username(request.adminUsername())
                .email(request.adminEmail())
                .firstName(request.adminFirstName())
                .lastName(request.adminLastName())
                .build();
        admin.setEnterpriseId(enterprise.getId());
        appUserRepository.save(admin);

        return EnterpriseResponse.from(enterprise);
    }

    public List<EnterpriseResponse> findAll() {
        return enterpriseRepository.findAll().stream().map(EnterpriseResponse::from).toList();
    }

    public EnterpriseResponse findById(Long id) {
        return enterpriseRepository.findById(id)
                .map(EnterpriseResponse::from)
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + id));
    }
}
