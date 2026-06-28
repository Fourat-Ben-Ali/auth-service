package com.example.demo.controller;

import com.example.demo.dto.EnterpriseRequest;
import com.example.demo.dto.EnterpriseResponse;
import com.example.demo.service.EnterpriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnterpriseResponse> create(@Valid @RequestBody EnterpriseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enterpriseService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EnterpriseResponse>> findAll() {
        return ResponseEntity.ok(enterpriseService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnterpriseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(enterpriseService.findById(id));
    }
}
