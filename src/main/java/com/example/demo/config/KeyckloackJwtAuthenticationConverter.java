package com.example.demo.config;

import com.example.demo.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class KeyckloackJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAuthorityService userAuthorityService;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Collection<GrantedAuthority> dbAuthorities = userAuthorityService.getAuthorities(source.getSubject());

        Collection<GrantedAuthority> keycloakAuthorities = Stream.of(
                new JwtGrantedAuthoritiesConverter().convert(source),
                extractRealmRoles(source),
                extractResourceRoles(source)
        ).flatMap(Collection::stream).collect(Collectors.toSet());

        Collection<GrantedAuthority> all = Stream.concat(dbAuthorities.stream(), keycloakAuthorities.stream())
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(source, all);
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
