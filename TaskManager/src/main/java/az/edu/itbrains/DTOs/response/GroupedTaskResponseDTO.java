package az.edu.itbrains.DTOs.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupedTaskResponseDTO {
    private LocalDate date;
    private List<TaskResponseDTO> tasks;
}