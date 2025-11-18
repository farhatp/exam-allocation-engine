package com.example.examallocation.repository;

import com.example.examallocation.model.Candidate;
import com.example.examallocation.model.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {
    List<CandidateApplication> findByStatus(CandidateApplication.Status status);
    List<CandidateApplication> findByCandidate(Candidate candidate);
}
