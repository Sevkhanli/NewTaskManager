package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionRequestDTO {

    @NotNull(message = "Task ID boş ola bilməz")
    private Long taskId;

    @NotBlank(message = "Tamamlama izahatı boş ola bilməz")
    private String completionDescription;

    private String evidenceLink;  // Proof link (screenshot, file, etc.)
}
