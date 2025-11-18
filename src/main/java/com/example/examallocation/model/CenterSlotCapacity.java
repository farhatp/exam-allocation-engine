package com.example.examallocation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"center_id", "slot_id"}))
public class CenterSlotCapacity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Center center;

    @ManyToOne(optional = false)
    private ExamSlot slot;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int used = 0;

    public int getRemaining() {
        return capacity - used;
    }

    public void incrementUsed() {
        this.used += 1;
    }
}
