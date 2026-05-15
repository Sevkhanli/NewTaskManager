package az.edu.itbrains.DTOs.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyConfigRequestDTO {

    @NotNull(message = "Deadline keçəndə cərimə məbləği boş ola bilməz")
    @Positive(message = "Məbləğ müsbət olmalıdır")
    private Double deadlineMissedAmount;

    @NotNull(message = "Status tamamlanmayanda cərimə məbləği boş ola bilməz")
    @Positive(message = "Məbləğ müsbət olmalıdır")
    private Double statusNotCompletedAmount;

    @NotNull(message = "Yalançı tamamlama cərimə məbləği boş ola bilməz")
    @Positive(message = "Məbləğ müsbət olmalıdır")
    private Double falseCompletionAmount;

    private String currency = "AZN";
}
