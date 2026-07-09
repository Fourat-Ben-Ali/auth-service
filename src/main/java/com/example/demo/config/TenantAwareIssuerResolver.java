package com.example.demo.config;

import com.example.demo.repository.EnterpriseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the right JwtDecoder per request based on the token's issuer —
 * "master" (platform super admin) or any realm matching a known enterprise
 * slug. This is what makes per-enterprise Keycloak realms actually work:
 * without it, Spring Security only ever trusts one fixed issuer.
 */
@Component
@RequiredArgsConstructor
public class TenantAwareIssuerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.master-realm}")
    private String masterRealm;

    private final EnterpriseRepository enterpriseRepository;
    private final KeyckloackJwtAuthenticationConverter jwtConverter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, AuthenticationManager> managers = new ConcurrentHashMap<>();

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String issuer = extractIssuer(request);
        if (issuer == null || !isTrustedIssuer(issuer)) {
            throw new InvalidBearerTokenException("Unknown or untrusted token issuer");
        }
        return managers.computeIfAbsent(issuer, this::buildAuthenticationManager);
    }

    private AuthenticationManager buildAuthenticationManager(String issuer) {
        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(jwtConverter);
        return provider::authenticate;
    }

    private boolean isTrustedIssuer(String issuer) {
        String prefix = serverUrl + "/realms/";
        if (!issuer.startsWith(prefix)) {
            return false;
        }
        String realm = issuer.substring(prefix.length());
        return realm.equals(masterRealm) || enterpriseRepository.existsBySlug(realm);
    }

    @SuppressWarnings("unchecked")
    private String extractIssuer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = header.substring(7);
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            Object iss = claims.get("iss");
            return iss != null ? iss.toString() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
