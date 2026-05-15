package az.edu.itbrains.DTOs.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminTaskRequestDTO extends UserTaskRequestDTO {
    private Long assigneeId;
}