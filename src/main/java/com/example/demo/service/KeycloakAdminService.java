package com.example.demo.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

/** Per-user Keycloak operations, always scoped to the caller's own enterprise realm. */
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloak;

    public String createUser(String realm, String username, String email, String firstName,
                              String lastName, String password) {
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

        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() == 201) {
                String location = response.getHeaderString("Location");
                return location.substring(location.lastIndexOf('/') + 1);
            }
            throw new RuntimeException("Failed to create Keycloak user: HTTP " + response.getStatus()
                    + " — " + response.readEntity(String.class));
        }
    }

    public void updateProfile(String realm, String keycloakId, String email, String firstName, String lastName) {
        UserRepresentation user = keycloak.realm(realm).users().get(keycloakId).toRepresentation();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        keycloak.realm(realm).users().get(keycloakId).update(user);
    }

    public void deleteUser(String realm, String keycloakId) {
        keycloak.realm(realm).users().get(keycloakId).remove();
    }

    public void setEnabled(String realm, String keycloakId, boolean enabled) {
        UserRepresentation user = keycloak.realm(realm).users().get(keycloakId).toRepresentation();
        user.setEnabled(enabled);
        keycloak.realm(realm).users().get(keycloakId).update(user);
    }
}
