package com.example.examallocation.controller;

import com.example.examallocation.model.Allocation;
import com.example.examallocation.model.Candidate;
import com.example.examallocation.repository.AllocationRepository;
import com.example.examallocation.repository.CandidateRepository;
import com.example.examallocation.service.AllocationService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Operation(summary = "Allocate candidates to exam centers", description = "Allocates all registered candidates to available exam centers based on predefined criteria.")
    @PostMapping("/allocate")
    public ResponseEntity<?> allocate() {
        AllocationService.AllocationResult result = allocationService.allocateAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get allocation details for a candidate", description = "Retrieves the allocation details for a candidate based on their registration number.")
    @GetMapping("/allocation/{registrationNumber}")
    public ResponseEntity<?> getAllocation(@PathVariable String registrationNumber) {
        Candidate candidate = candidateRepository.findByRegistrationNumber(registrationNumber)
                .orElse(null);
        if (candidate == null) {
           // return ResponseEntity.notFound().build();
           return ResponseEntity.ok("Candidate Not found");
        }
        List<Allocation> allocations = allocationRepository.findByCandidateApplication_Candidate(candidate);
        if(!allocations.isEmpty())
            return ResponseEntity.ok(allocations);
        else{
            Map<String, Object> response = new HashMap<>();  
            response.put("status", "Allocation Pending");
            response.put("candidate", candidate);
            return ResponseEntity.ok(response);
        }
            
    }
}
