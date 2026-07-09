package com.example.demo.controller;

import com.example.demo.dto.AssignRolesRequest;
import com.example.demo.dto.SetEnabledRequest;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> setEnabled(@PathVariable Long id, @Valid @RequestBody SetEnabledRequest request) {
        return ResponseEntity.ok(userService.setEnabled(id, request));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_ASSIGN_ROLE') or hasRole('SUPER_ADMIN') or hasRole('PLATFORM_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> assignRoles(@PathVariable Long id,
                                                    @Valid @RequestBody AssignRolesRequest request) {
        return ResponseEntity.ok(userService.assignRoles(id, request));
    }
}
