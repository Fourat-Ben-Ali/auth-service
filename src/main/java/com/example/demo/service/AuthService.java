package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * A blank enterpriseSlug means "platform super admin" — routed to Keycloak's
 * master realm. A slug routes to that enterprise's own realm. Callers never
 * talk to Keycloak directly; this is the only path in.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.master-realm}")
    private String masterRealm;

    @Value("${keycloak.admin.client-id}")
    private String masterClientId;

    private final RestTemplate restTemplate;

    public Map<?, ?> login(LoginRequest request) {
        boolean platformLogin = !StringUtils.hasText(request.enterpriseSlug());
        String realm = platformLogin ? masterRealm : request.enterpriseSlug();
        String clientId = platformLogin ? masterClientId : KeycloakRealmAdminService.TENANT_CLIENT_ID;

        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("username", request.username());
        body.add("password", request.password());

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), Map.class);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }
}
