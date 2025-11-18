package com.example.examallocation.repository;

import com.example.examallocation.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByRegistrationNumber(String registrationNumber);
}
