package com.example.examallocation.controller;

import com.example.examallocation.model.Allocation;
import com.example.examallocation.model.Candidate;
import com.example.examallocation.repository.AllocationRepository;
import com.example.examallocation.repository.CandidateRepository;
import com.example.examallocation.service.AllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AllocationController {

    private final AllocationService allocationService;
    private final CandidateRepository candidateRepository;
    private final AllocationRepository allocationRepository;

    public AllocationController(AllocationService allocationService,
                                CandidateRepository candidateRepository,
                                AllocationRepository allocationRepository) {
        this.allocationService = allocationService;
        this.candidateRepository = candidateRepository;
        this.allocationRepository = allocationRepository;
    }

    @PostMapping("/allocate")
    public ResponseEntity<?> allocate() {
        AllocationService.AllocationResult result = allocationService.allocateAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/allocation/{registrationNumber}")
    public ResponseEntity<?> getAllocation(@PathVariable String registrationNumber) {
        Candidate candidate = candidateRepository.findByRegistrationNumber(registrationNumber)
                .orElse(null);
        if (candidate == null) {
            return ResponseEntity.notFound().build();
        }
        List<Allocation> allocations = allocationRepository.findByCandidateApplication_Candidate(candidate);
        return ResponseEntity.ok(allocations);
    }
}
