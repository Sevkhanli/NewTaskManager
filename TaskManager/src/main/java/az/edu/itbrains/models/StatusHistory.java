package az.edu.itbrains.models;

import az.edu.itbrains.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "status_history")
public class StatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Enumerated(EnumType.STRING)
    private TaskStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private TaskStatus newStatus;

    // Deadline dəyişərsə, bunu da saxlaya bilərik
    private LocalDateTime oldDeadline;
    private LocalDateTime newDeadline;

    private String changedBy; // Hansı istifadəçi dəyişdi? (Admin və ya User)

    private String reason;    // Niyə dəyişdi? (Məsələn: "Deadline uzadıldı")

    private LocalDateTime changedAt;
}