package az.edu.itbrains.DTOs.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserListDTO {
    private Long id;
    private String fullName;
}