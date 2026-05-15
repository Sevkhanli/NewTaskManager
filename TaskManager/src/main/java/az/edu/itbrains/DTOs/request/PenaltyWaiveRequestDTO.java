package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyWaiveRequestDTO {

    @NotNull(message = "Cərimə ID boş ola bilməz")
    private Long penaltyId;

    @NotBlank(message = "Bağışlama səbəbi boş ola bilməz")
    private String waiveReason;
}
