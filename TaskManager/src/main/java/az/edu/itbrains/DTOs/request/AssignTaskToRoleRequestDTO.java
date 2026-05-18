package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignTaskToRoleRequestDTO {
    @NotBlank(message = "Title boş ola bilməz")
    private String title;

    private String description;
    private LocalDateTime deadline;

    @NotBlank(message = "Rol adı boş ola bilməz")
    private String roleName; // Məsələn: "SATIS", "DEVELOPER" və s.
}