package com.example.demo.controller;

import com.example.demo.dto.PermissionRequest;
import com.example.demo.dto.PermissionResponse;
import com.example.demo.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PermissionResponse>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PermissionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }
}
