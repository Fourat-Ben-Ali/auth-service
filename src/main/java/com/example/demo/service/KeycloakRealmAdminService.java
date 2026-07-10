package com.example.demo.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provisions real Keycloak realms — one per enterprise/tenant. Every tenant
 * user, role and permission check in this app is only ever reached through
 * this service; nothing outside auth-service talks to Keycloak directly.
 */
@Service
@RequiredArgsConstructor
public class KeycloakRealmAdminService {

    public static final String TENANT_CLIENT_ID = "app-client";

    private final Keycloak keycloak;

    public void createRealm(String realmName, String displayName) {
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setDisplayName(displayName);
        realm.setEnabled(true);
        keycloak.realms().create(realm);
    }

    /**
     * Creates the realm's direct-access client and stamps every token it
     * issues with a hardcoded {@code enterprise_id} claim. This is what lets
     * downstream services (e.g. file-storage-service) enforce tenant
     * isolation from the JWT alone, without calling back into auth-service.
     */
    public void createDirectAccessClient(String realmName, Long enterpriseId) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(TENANT_CLIENT_ID);
        client.setEnabled(true);
        client.setPublicClient(true);
        client.setDirectAccessGrantsEnabled(true);
        client.setStandardFlowEnabled(false);
        client.setRedirectUris(List.of("*"));
        client.setProtocolMappers(List.of(enterpriseIdClaimMapper(enterpriseId)));
        keycloak.realm(realmName).clients().create(client);
    }

    private ProtocolMapperRepresentation enterpriseIdClaimMapper(Long enterpriseId) {
        ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
        mapper.setName("enterprise_id");
        mapper.setProtocol("openid-connect");
        mapper.setProtocolMapper("oidc-hardcoded-claim-mapper");

        Map<String, String> config = new HashMap<>();
        config.put("claim.name", "enterprise_id");
        config.put("claim.value", String.valueOf(enterpriseId));
        config.put("jsonType.label", "long");
        config.put("access.token.claim", "true");
        config.put("id.token.claim", "true");
        config.put("userinfo.token.claim", "true");
        mapper.setConfig(config);

        return mapper;
    }

    public void createRealmRole(String realmName, String roleName, String description) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription(description);
        keycloak.realm(realmName).roles().create(role);
    }

    /** Creates a user in the given realm and assigns them a realm role. Returns the Keycloak user id. */
    public String createUserWithRealmRole(String realmName, String username, String email,
                                           String firstName, String lastName, String password,
                                           String roleName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        String userId;
        try (Response response = keycloak.realm(realmName).users().create(user)) {
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create Keycloak user: HTTP " + response.getStatus()
                        + " — " + response.readEntity(String.class));
            }
            String location = response.getHeaderString("Location");
            userId = location.substring(location.lastIndexOf('/') + 1);
        }

        RoleRepresentation role = keycloak.realm(realmName).roles().get(roleName).toRepresentation();
        keycloak.realm(realmName).users().get(userId).roles().realmLevel().add(List.of(role));

        return userId;
    }

    public boolean realmExists(String realmName) {
        return keycloak.realms().findAll().stream().anyMatch(r -> r.getRealm().equals(realmName));
    }
}
