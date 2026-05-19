package az.edu.itbrains.services;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.AssignTaskToRoleRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    TaskResponseDTO createMyTask(UserTaskRequestDTO request);
    TaskResponseDTO createTaskAsAdmin(AdminTaskRequestDTO request);
    List<TaskResponseDTO> getAllActiveTasks();
    List<TaskResponseDTO> getAllTasksWithAdvancedFilters(String role, TaskStatus status, LocalDateTime startDate, LocalDateTime endDate);
    TaskResponseDTO getTaskById(Long id);
    void createTaskForRole(AssignTaskToRoleRequestDTO request);

    // Qruplaşdırma metodu filtrləri qəbul edəcək şəkildə yeniləndi
    List<GroupedTaskResponseDTO> getTasksGroupedByDate(String role, TaskStatus status, LocalDateTime startDate, LocalDateTime endDate);

    TaskResponseDTO updateTask(Long id, UserTaskRequestDTO request);
    TaskResponseDTO updateTaskByAdmin(Long id, AdminTaskRequestDTO request);
    TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason);
    String deleteTask(Long taskId);
}