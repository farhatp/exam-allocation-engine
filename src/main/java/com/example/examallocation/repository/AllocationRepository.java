package com.example.examallocation.repository;

import com.example.examallocation.model.Allocation;
import com.example.examallocation.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByCandidateApplication_Candidate(Candidate candidate);
}
