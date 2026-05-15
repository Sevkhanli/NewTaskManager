package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendRequestDTO {
    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda deyil")
    private String email;
}
