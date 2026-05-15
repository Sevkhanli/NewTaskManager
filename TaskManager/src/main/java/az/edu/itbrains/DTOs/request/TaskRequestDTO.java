package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {
    @NotBlank(message = "Title boş ola bilməz")
    private String title;

    private String description;
    private LocalDateTime deadline;

    // Artıq @NotNull deyil.
    // Əgər null gəlsə, Servis qatında "currentUser" olaraq təyin edəcəyik.
    private Long assigneeId;
}