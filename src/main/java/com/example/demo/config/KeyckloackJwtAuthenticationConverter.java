package com.example.demo.config;

import com.example.demo.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class KeyckloackJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.master-realm}")
    private String masterRealm;

    private final UserAuthorityService userAuthorityService;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Collection<GrantedAuthority> dbAuthorities = userAuthorityService.getAuthorities(source.getSubject());

        Collection<GrantedAuthority> keycloakAuthorities = Stream.of(
                new JwtGrantedAuthoritiesConverter().convert(source),
                extractRealmRoles(source),
                extractResourceRoles(source)
        ).flatMap(Collection::stream).collect(Collectors.toSet());

        Collection<GrantedAuthority> all = new HashSet<>();
        all.addAll(dbAuthorities);
        all.addAll(keycloakAuthorities);

        // Tokens issued by the master realm come from a platform super admin —
        // grant a distinct authority so tenant super admins can't reach
        // platform-only endpoints (e.g. creating enterprises) just by also
        // holding a SUPER_ADMIN role in their own realm.
        if (isMasterRealmIssuer(source)) {
            all.add(new SimpleGrantedAuthority("ROLE_PLATFORM_SUPER_ADMIN"));
        }

        return new JwtAuthenticationToken(source, all);
    }

    private boolean isMasterRealmIssuer(Jwt jwt) {
        Object issuer = jwt.getClaim("iss");
        return issuer != null && issuer.toString().equals(serverUrl + "/realms/" + masterRealm);
    }

    // realm_access.roles → ROLE_SUPER_ADMIN, etc.
    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptySet();

        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) return Collections.emptySet();

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
                .collect(Collectors.toSet());
    }

    // resource_access.account.roles → client-level roles
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) return Collections.emptySet();

        Map<String, List<String>> account = (Map<String, List<String>>) resourceAccess.get("account");
        if (account == null) return Collections.emptySet();

        List<String> roles = account.get("roles");
        if (roles == null) return Collections.emptySet();

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_")))
                .collect(Collectors.toSet());
    }
}
