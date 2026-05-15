package az.edu.itbrains.DTOs.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPenaltySummaryDTO {
    private Long userId;
    private String userName;
    private Double totalPendingAmount;
    private Long totalPenalties;
    private Long pendingPenalties;
    private Long paidPenalties;
    private String currency;
}
