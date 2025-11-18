package com.example.examallocation.repository;

import com.example.examallocation.model.Center;
import com.example.examallocation.model.CenterSlotCapacity;
import com.example.examallocation.model.ExamSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CenterSlotCapacityRepository extends JpaRepository<CenterSlotCapacity, Long> {
    Optional<CenterSlotCapacity> findByCenterAndSlot(Center center, ExamSlot slot);
    List<CenterSlotCapacity> findByCenter(Center center);
}
