package az.edu.itbrains.DTOs.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyConfigResponseDTO {
    private Long id;
    private Double deadlineMissedAmount;
    private Double statusNotCompletedAmount;
    private Double falseCompletionAmount;
    private String currency;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedByName;
}
