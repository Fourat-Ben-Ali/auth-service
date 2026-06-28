package com.example.demo.service;

import com.example.demo.dto.PermissionRequest;
import com.example.demo.dto.PermissionResponse;
import com.example.demo.entity.Permission;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.existsByName(request.name())) {
            throw new ConflictException("Permission '" + request.name() + "' already exists");
        }
        Permission permission = Permission.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream().map(PermissionResponse::from).toList();
    }

    public PermissionResponse findById(Long id) {
        return permissionRepository.findById(id)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
    }
}
