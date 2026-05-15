package az.edu.itbrains.services;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import java.util.List;

public interface TaskService {
    TaskResponseDTO createMyTask(UserTaskRequestDTO request);
    TaskResponseDTO createTaskAsAdmin(AdminTaskRequestDTO request);
    List<TaskResponseDTO> getAllActiveTasks();
    TaskResponseDTO getTaskById(Long id);

    List<GroupedTaskResponseDTO> getTasksGroupedByDate();

    // User-lər üçün update
    TaskResponseDTO updateTask(Long id, UserTaskRequestDTO request);
    // Admin-lər üçün update (Assignee dəyişmək imkanı ilə)
    TaskResponseDTO updateTaskByAdmin(Long id, AdminTaskRequestDTO request);

    TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason);
    String deleteTask(Long taskId);
}