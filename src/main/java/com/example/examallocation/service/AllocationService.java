package com.example.examallocation.service;

import com.example.examallocation.model.*;
import com.example.examallocation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final CandidateApplicationRepository candidateApplicationRepository;
    private final CenterRepository centerRepository;
    private final ExamSlotRepository examSlotRepository;
    private final CenterSlotCapacityRepository centerSlotCapacityRepository;
    private final AllocationRepository allocationRepository;

    public AllocationService(CandidateApplicationRepository candidateApplicationRepository,
                             CenterRepository centerRepository,
                             ExamSlotRepository examSlotRepository,
                             CenterSlotCapacityRepository centerSlotCapacityRepository,
                             AllocationRepository allocationRepository) {
        this.candidateApplicationRepository = candidateApplicationRepository;
        this.centerRepository = centerRepository;
        this.examSlotRepository = examSlotRepository;
        this.centerSlotCapacityRepository = centerSlotCapacityRepository;
        this.allocationRepository = allocationRepository;
    }

    public record AllocationResult(long allocated, long pending) {}

    @Transactional
    public AllocationResult allocateAll() {
        List<CandidateApplication> pendingApps =
                candidateApplicationRepository.findByStatus(CandidateApplication.Status.PENDING);

        Map<Candidate, List<CandidateApplication>> byCandidate =
                pendingApps.stream().collect(Collectors.groupingBy(CandidateApplication::getCandidate));

        long allocatedCount = 0;
        long pendingCount = 0;

        List<Center> centers = centerRepository.findAll();
        List<ExamSlot> allSlots = examSlotRepository.findAll();

        for (Map.Entry<Candidate, List<CandidateApplication>> entry : byCandidate.entrySet()) {
            Candidate candidate = entry.getKey();
            List<CandidateApplication> applications = entry.getValue();

            boolean allocated = allocateForCandidate(candidate, applications, centers, allSlots);
            if (allocated) {
                allocatedCount += applications.size();
            } else {
                // create pending allocation records
                for (CandidateApplication app : applications) {
                    Allocation allocation = new Allocation();
                    allocation.setCandidateApplication(app);
                    allocation.setStatus(Allocation.Status.PENDING);
                    allocationRepository.save(allocation);
                    pendingCount++;
                }
            }
        }

        return new AllocationResult(allocatedCount, pendingCount);
    }

    private boolean allocateForCandidate(Candidate candidate,
                                         List<CandidateApplication> applications,
                                         List<Center> centers,
                                         List<ExamSlot> allSlots) {

        List<Center> allowedCenters = centers.stream()
                .filter(c -> !candidate.isPwd() || c.isPwdFriendly())
                .toList();

        List<ExamSlot> allowedSlots = allSlots.stream()
                .filter(slot -> candidate.getGender() == Candidate.Gender.M || !isLastSlotOfDay(slot, allSlots))
                .toList();

        if (applications.size() == 1) {
            return allocateSingle(candidate, applications.get(0), allowedCenters, allowedSlots);
        } else {
            return allocateMultiple(candidate, applications, allowedCenters, allowedSlots);
        }
    }

    private boolean isLastSlotOfDay(ExamSlot slot, List<ExamSlot> allSlots) {
        // Simple heuristic: slot with maximum time on its date is last slot
        return allSlots.stream()
                .filter(s -> s.getExamDate().equals(slot.getExamDate()))
                .max(Comparator.comparing(ExamSlot::getSlotTime))
                .map(last -> last.getId().equals(slot.getId()))
                .orElse(false);
    }

    private boolean allocateSingle(Candidate candidate,
                                   CandidateApplication app,
                                   List<Center> centers,
                                   List<ExamSlot> slots) {
        for (Center center : centers) {
            for (ExamSlot slot : slots) {
                Optional<CenterSlotCapacity> optCsc =
                        centerSlotCapacityRepository.findByCenterAndSlot(center, slot);
                if (optCsc.isEmpty()) continue;
                CenterSlotCapacity csc = optCsc.get();
                if (csc.getRemaining() > 0) {
                    csc.incrementUsed();
                    centerSlotCapacityRepository.save(csc);

                    Allocation allocation = new Allocation();
                    allocation.setCandidateApplication(app);
                    allocation.setCenter(center);
                    allocation.setSlot(slot);
                    allocation.setStatus(Allocation.Status.ALLOCATED);
                    allocationRepository.save(allocation);

                    app.setStatus(CandidateApplication.Status.ALLOCATED);
                    candidateApplicationRepository.save(app);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allocateMultiple(Candidate candidate,
                                     List<CandidateApplication> apps,
                                     List<Center> centers,
                                     List<ExamSlot> slots) {

        int n = apps.size();

        for (Center center : centers) {
            Map<java.time.LocalDate, List<ExamSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(ExamSlot::getExamDate));

            for (Map.Entry<java.time.LocalDate, List<ExamSlot>> entry : slotsByDate.entrySet()) {
                List<ExamSlot> daySlots = entry.getValue().stream()
                        .sorted(Comparator.comparing(ExamSlot::getSlotTime))
                        .toList();

                if (daySlots.size() < n) continue;

                List<ExamSlot> pickedSlots = new ArrayList<>();
                for (ExamSlot s : daySlots) {
                    Optional<CenterSlotCapacity> optCsc =
                            centerSlotCapacityRepository.findByCenterAndSlot(center, s);
                    if (optCsc.isPresent() && optCsc.get().getRemaining() > 0) {
                        pickedSlots.add(s);
                    }
                    if (pickedSlots.size() == n) break;
                }

                if (pickedSlots.size() == n) {
                    for (int i = 0; i < n; i++) {
                        CandidateApplication app = apps.get(i);
                        ExamSlot slot = pickedSlots.get(i);
                        CenterSlotCapacity csc =
                                centerSlotCapacityRepository.findByCenterAndSlot(center, slot).orElseThrow();
                        csc.incrementUsed();
                        centerSlotCapacityRepository.save(csc);

                        Allocation allocation = new Allocation();
                        allocation.setCandidateApplication(app);
                        allocation.setCenter(center);
                        allocation.setSlot(slot);
                        allocation.setStatus(Allocation.Status.ALLOCATED);
                        allocationRepository.save(allocation);

                        app.setStatus(CandidateApplication.Status.ALLOCATED);
                        candidateApplicationRepository.save(app);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
