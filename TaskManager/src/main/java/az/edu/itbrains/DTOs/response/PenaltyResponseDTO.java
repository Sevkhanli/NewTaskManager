package az.edu.itbrains.DTOs.response;

import az.edu.itbrains.enums.PenaltyStatus;
import az.edu.itbrains.enums.PenaltyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyResponseDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("taskId")
    private Long taskId;
    
    @JsonProperty("taskTitle")
    private String taskTitle;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("penaltyType")
    private PenaltyType penaltyType;
    
    @JsonProperty("status")
    private PenaltyStatus status;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("daysOverdue")
    private Integer daysOverdue;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("evidenceRequired")
    private boolean evidenceRequired;
    
    @JsonProperty("evidenceProvided")
    private boolean evidenceProvided;
    
    @JsonProperty("evidenceDescription")
    private String evidenceDescription;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("paidAt")
    private LocalDateTime paidAt;
}
