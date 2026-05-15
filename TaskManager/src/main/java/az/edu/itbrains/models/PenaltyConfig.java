package az.edu.itbrains.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalty_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deadline_missed_amount", nullable = false)
    private Double deadlineMissedAmount;  // Deadline keçəndə (gündəlik)

    @Column(name = "status_not_completed_amount", nullable = false)
    private Double statusNotCompletedAmount;  // Status tamamlanmayanda (bir dəfəlik)

    @Column(name = "false_completion_amount", nullable = false)
    private Double falseCompletionAmount;  // Yalançı tamamlama (bir dəfəlik)

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;  // AZN, USD, etc.

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;  // Son dəyişən admin
}
