package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskRequestDTO {
    @NotBlank(message = "Title boş ola bilməz")
    private String title;
    private String description;
    private LocalDateTime deadline;
}