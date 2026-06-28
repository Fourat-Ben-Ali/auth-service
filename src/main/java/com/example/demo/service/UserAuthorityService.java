package com.example.demo.service;

import com.example.demo.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAuthorityService {

    private final AppUserRepository appUserRepository;

    public Collection<GrantedAuthority> getAuthorities(String keycloakId) {
        return appUserRepository.findByKeycloakId(keycloakId)
                .map(user -> {
                    Set<GrantedAuthority> authorities = new HashSet<>();
                    user.getRoles().forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                        role.getPermissions().forEach(perm ->
                                authorities.add(new SimpleGrantedAuthority(perm.getName())));
                    });
                    return (Collection<GrantedAuthority>) authorities;
                })
                .orElse(Set.of());
    }
}
