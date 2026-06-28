package com.example.demo.service;

import com.example.demo.dto.EnterpriseRequest;
import com.example.demo.dto.EnterpriseResponse;
import com.example.demo.entity.Enterprise;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;

    public EnterpriseResponse create(EnterpriseRequest request) {
        if (enterpriseRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Enterprise with slug '" + request.slug() + "' already exists");
        }
        Enterprise enterprise = Enterprise.builder()
                .name(request.name())
                .slug(request.slug())
                .build();
        return EnterpriseResponse.from(enterpriseRepository.save(enterprise));
    }

    public List<EnterpriseResponse> findAll() {
        return enterpriseRepository.findAll().stream().map(EnterpriseResponse::from).toList();
    }

    public EnterpriseResponse findById(Long id) {
        return enterpriseRepository.findById(id)
                .map(EnterpriseResponse::from)
                .orElseThrow(() -> new NotFoundException("Enterprise not found: " + id));
    }
}
