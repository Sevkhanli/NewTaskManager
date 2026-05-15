package az.edu.itbrains.models;

import az.edu.itbrains.enums.PenaltyStatus;
import az.edu.itbrains.enums.PenaltyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Cərimə çəkən user

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;  // Əlaqəli task

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type", nullable = false)
    private PenaltyType penaltyType;  // Cərimə tipi

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.PENDING;

    @Column(name = "amount", nullable = false)
    private Double amount;  // Cərimə məbləği

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;  // Valyuta (AZN)

    @Column(name = "days_overdue")
    private Integer daysOverdue;  // Neçə gün gecikib (DEADLINE_MISSED üçün)

    @Column(name = "description", length = 1000)
    private String description;  // Cərimə səbəbi

    @Column(name = "evidence_required")
    @Builder.Default
    private boolean evidenceRequired = false;  // Proof tələb olunurmu

    @Column(name = "evidence_provided")
    @Builder.Default
    private boolean evidenceProvided = false;  // Proof verilibmi

    @Column(name = "evidence_description", length = 2000)
    private String evidenceDescription;  // Userin izahatı

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;  // Ödənmə tarixi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by")
    private User waivedBy;  // Bağışlayan admin

    @Column(name = "waived_at")
    private LocalDateTime waivedAt;  // Bağışlanma tarixi

    @Column(name = "waive_reason", length = 500)
    private String waiveReason;  // Bağışlama səbəbi
}
