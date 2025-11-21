package com.example.examallocation.service;

import com.example.examallocation.model.*;
import com.example.examallocation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public record AllocationResult(long allocated, long pending, LocalDateTime runAt) {}

    @Transactional
    public AllocationResult allocateAll() {
        
        // Exam Seat Allocation Engine Starts

        //fetch the list of all the candidates which are yet 
        // to be allocated by the engine
        List<CandidateApplication> pendingApps =
                candidateApplicationRepository.findByStatus(CandidateApplication.Status.PENDING);
        
        // Make a Map of each candidate with a list of his own applications
         Map<Candidate, List<CandidateApplication>> byCandidate =
                pendingApps.stream().collect(Collectors.groupingBy(CandidateApplication::getCandidate));

        // Initialize counts to 0
        long allocatedCount = 0;
        long pendingCount = 0;
       
        // fetch the list of centers
        List<Center> centers = centerRepository.findAll();
        
        // fetch the list of all the exam slots
        List<ExamSlot> allSlots = examSlotRepository.findAll();

        // process one candidate and all of his applications 
        // for multiple positions in one go 
        // i.e. assign a center and a different exam slot for 
        // each application of that candidate   
        for (Map.Entry<Candidate, List<CandidateApplication>> entry : byCandidate.entrySet()) {
            
            // single candidate
            Candidate candidate = entry.getKey();
            
            // list of one or more applications of a single candidate
            List<CandidateApplication> applications = entry.getValue();
            
            // call a method to allocate single candidate and all his
            // applications from the available centers and slots
            // returns true on successful allocation else false
            boolean allocated = allocateForCandidate(candidate, applications, centers, allSlots);
            
            // when allocation is successful, allocatedCount is 
            // incremented by the count of applications of a single 
            // processed candidate
            if (allocated) {
                allocatedCount += applications.size();
            } 

            // when allocation is not successful, status of candidate  
            // application(s) is/are set to pending. Also, the status 
            // of allocation is set as pending for all the candidate 
            // applications(s) of that candidate and the pending count 
            // is incremented by 1 for each application of that single 
            // candidate
            else {
                // create pending allocation records
                for (CandidateApplication app : applications) {
                    Allocation allocation = new Allocation();
                    allocation.setCandidateApplication(app);
                    allocation.setStatus(Allocation.Status.PENDING);
                    allocationRepository.save(allocation);
                    pendingCount++;
                }
            }
            // repeat this process for each candidate in the map
        }
        // Exam Seat Allocation Engine Ends

        // Respond with the statistics of exam seats allocated by the 
        // engine and the exam seats pending to be allocated 
        return new AllocationResult(allocatedCount, pendingCount, LocalDateTime.now());
    }

    private boolean allocateForCandidate(Candidate candidate,
                                         List<CandidateApplication> applications,
                                         List<Center> centers,
                                         List<ExamSlot> allSlots) {
        
        // filter the list of allowed centers for this candidate
        // i.e. if this candidate is pwd then filter only the pwd 
        // friendly centers and ignore the rest however, if the 
        // candidate is not pwd then, consider all the centers. 
        // Hence, OR operator is used for filtering only pwd 
        // friendly centers for pwd candidates and all the centers 
        // for non-pwd candidates
        List<Center> allowedCenters = centers.stream()
                .filter(c -> !candidate.isPwd() || c.isPwdFriendly())
                .toList();

        // filter the allowed slots for this candidate 
        // i.e. if the candidate is female then, last slot is 
        // not allowed for her so ignore the last slot for female 
        // candidates and fetch the rest. For male candidates, fetch 
        // all slots  
        List<ExamSlot> allowedSlots = allSlots.stream()
                .filter(slot -> candidate.getGender() == Candidate.Gender.M || !isLastSlotOfDay(slot, allSlots))
                .toList();

        // if candidate has just applied for one post then,
        // go with the single allocation flow for that candidate
        if (applications.size() == 1) {
            // single candidate, single application, 
            // list of allowed centers, list of allowed slots
            return allocateSingle(candidate, applications.get(0), allowedCenters, allowedSlots);
        } 

        // if candidate has applied for more than one post then,
        // go with the multiple allocation flow for that candidate
        else {
            // single candidate, list of candidate applications, 
            // list of allowed centers, list of allowed slots
            return allocateMultiple(candidate, applications, allowedCenters, allowedSlots);
        }
    }

    private boolean isLastSlotOfDay(ExamSlot slot, List<ExamSlot> allSlots) {
        // Simple heuristic: slot with maximum time on its date is last slot
        return allSlots.stream()
                .filter(s -> s.getExamDate().equals(slot.getExamDate()))
                .max(Comparator.comparing(ExamSlot::getSlotStartTime))
                .map(last -> last.getId().equals(slot.getId()))
                .orElse(false);
    }

    private boolean allocateSingle(Candidate candidate,
                                   CandidateApplication app,
                                   List<Center> centers,
                                   List<ExamSlot> slots) {
        for (Center center : centers) {
            for (ExamSlot slot : slots) {
                // find the capacity of each center for each slot
                Optional<CenterSlotCapacity> optCsc =
                        centerSlotCapacityRepository.findByCenterAndSlot(center, slot);
                
                // if the capacity for that center and slot combination does'nt exist 
                // then, continue to find next
                if (optCsc.isEmpty()) continue;
               
                CenterSlotCapacity csc = optCsc.get();
                
                // if center capacity for the slot is remaining i.e. 
                // capacity - used > 0
                if (csc.getRemaining() > 0) {
                    
                    // increment used by 1
                    csc.incrementUsed();

                    // save this incremented used capacity
                    centerSlotCapacityRepository.save(csc);

                    // allocate the center and slot to the candidate 
                    // from allowed centers and slots where the 
                    // capacity was not full yet for that center and slot
                    // set status as allocated
                    Allocation allocation = new Allocation();
                    allocation.setCandidateApplication(app);
                    allocation.setCenter(center);
                    allocation.setSlot(slot);
                    allocation.setStatus(Allocation.Status.ALLOCATED);
                    allocationRepository.save(allocation);

                    // set the status of candidate application to 
                    // allocated from pending and return true on success
                    app.setStatus(CandidateApplication.Status.ALLOCATED);
                    candidateApplicationRepository.save(app);
                    return true;
                }
            }
        }
        // if the capacity is full and the candidate cannot be 
        // allocated then, return false
        return false;
    }

    private boolean allocateMultiple(Candidate candidate,
                                     List<CandidateApplication> apps,
                                     List<Center> centers,
                                     List<ExamSlot> slots) {
                                            
        int n = apps.size();

        // all the applications of a single candidate must be allocated
        // in a single center in a different time slot per application  
        // but for all his/her applications and that too on a same 
        // exam date 

        for (Center center : centers) {

            // Map the Exam Date to its list of slots on that day 
            Map<java.time.LocalDate, List<ExamSlot>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(ExamSlot::getExamDate));

            
            for (Map.Entry<java.time.LocalDate, List<ExamSlot>> entry : slotsByDate.entrySet()) {
                // for this Exam Date sort the list of exam slots in 
                // increasing order of slot start times
                List<ExamSlot> daySlots = entry.getValue().stream()
                        .sorted(Comparator.comparing(ExamSlot::getSlotStartTime))
                        .toList();

                // if the available slots on this day are less than 
                // the number of applications of a candidate then move 
                // on to the next exam day
                if (daySlots.size() < n) continue;

                List<ExamSlot> pickedSlots = new ArrayList<>();

                for (ExamSlot s : daySlots) {
                    // check if for this exam date and this slot 
                    // and for this center capacity exists
                    Optional<CenterSlotCapacity> optCsc =
                            centerSlotCapacityRepository.findByCenterAndSlot(center, s);
                    // if yes then, check if the capacity is remaining
                    // i.e. (capacity - used) > 0
                    if (optCsc.isPresent() && optCsc.get().getRemaining() > 0) {
                        // add this slot to the list of picked slots
                        pickedSlots.add(s);
                    }
                    // when the size of picked slots reaches the 
                    // number of applications of this candidate then, break
                    if (pickedSlots.size() == n) break;
                }

                if (pickedSlots.size() == n) {
                    // now we have equal number of applications 
                    // and slots 
                    for (int i = 0; i < n; i++) {
                        CandidateApplication app = apps.get(i);
                        ExamSlot slot = pickedSlots.get(i);
                        
                        // fetch center slot capacity of this center and slot
                        CenterSlotCapacity csc =
                                centerSlotCapacityRepository.findByCenterAndSlot(center, slot).orElseThrow();
                       
                        // increment used by 1 and update the used count
                        csc.incrementUsed();
                        centerSlotCapacityRepository.save(csc);
                        
                        // allocate the center and slot to this 
                        // candidate application and mark its 
                        // status as allocated
                        Allocation allocation = new Allocation();
                        allocation.setCandidateApplication(app);
                        allocation.setCenter(center);
                        allocation.setSlot(slot);
                        allocation.setStatus(Allocation.Status.ALLOCATED);
                        allocationRepository.save(allocation);

                        // mark candidate application as allocated 
                        // from pending 
                        app.setStatus(CandidateApplication.Status.ALLOCATED);
                        candidateApplicationRepository.save(app);
                    }
                    // return true when the candidate is allocated successfully
                    return true;
                }
            }
        }
        // return false if the candidate have not got allocated seat due 
        // to lack of exam slot capacity across centers 
        return false;
    }
}
