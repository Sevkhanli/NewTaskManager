package az.edu.itbrains.DTOs.response;

import az.edu.itbrains.enums.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime deadline;

    // İndi kimin yaratdığını və kimin icra etdiyini bilirik
    private String creatorName;
    private String assigneeName;
}