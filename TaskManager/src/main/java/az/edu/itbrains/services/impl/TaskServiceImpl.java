package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.AssignTaskToRoleRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.models.Task;
import az.edu.itbrains.models.User;
import az.edu.itbrains.repositories.PenaltyRepository;
import az.edu.itbrains.repositories.StatusHistoryRepository;
import az.edu.itbrains.repositories.TaskRepository;
import az.edu.itbrains.repositories.UserRepository;
import az.edu.itbrains.services.PenaltyService;
import az.edu.itbrains.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository historyRepository;
    private final PenaltyRepository penaltyRepository;
    private final PenaltyService penaltyService;
    private final ModelMapper modelMapper;

    // --- MƏRKƏZLƏŞDİRİLMİŞ İCAZƏ YOXLAMASI ---
    private void validatePermission(Task task, String actionType) {
        if (isAdmin()) return; // Admin hər şeyə icazəlidir

        boolean isCreator = isCreator(task);
        boolean isAssignee = isAssignee(task);

        switch (actionType) {
            case "READ":
            case "CHANGE_STATUS":
                if (isCreator || isAssignee) return;
                break;
            case "UPDATE":
            case "DELETE":
                if (isCreator) return;
                break;
        }
        throw new RuntimeException("Bu əməliyyatı icra etməyə icazəniz yoxdur!");
    }

    @Override
    @Transactional
    public TaskResponseDTO createMyTask(UserTaskRequestDTO request) {
        User currentUser = getCurrentUser();
        Task task = modelMapper.map(request, Task.class);
        task.setId(null);
        task.setCreator(currentUser);
        task.setAssignee(currentUser);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeleted(false);
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO createTaskAsAdmin(AdminTaskRequestDTO request) {
        Task task = modelMapper.map(request, Task.class);
        task.setId(null);
        task.setCreator(getCurrentUser());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee tapılmadı"));
        task.setAssignee(assignee);
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task tapılmadı"));
        validatePermission(task, "READ");
        return convertToResponse(task);
    }

    @Override
    @Transactional
    public void createTaskForRole(AssignTaskToRoleRequestDTO request) {
        List<User> usersInRole = userRepository.findByRoles_Name(request.getRoleName());
        if (usersInRole.isEmpty()) {
            throw new RuntimeException(request.getRoleName() + " roluna sahib heç bir istifadəçi tapılmadı!");
        }

        User adminUser = getCurrentUser();

        for (User user : usersInRole) {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDeadline(request.getDeadline());
            task.setCreator(adminUser);
            task.setAssignee(user);
            task.setStatus(TaskStatus.PENDING);
            task.setCreatedAt(LocalDateTime.now());
            task.setDeleted(false);
            taskRepository.save(task);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupedTaskResponseDTO> getTasksGroupedByDate() {
        List<TaskResponseDTO> allTasks = getAllActiveTasks();
        return allTasks.stream()
                .filter(task -> task.getDeadline() != null)
                .collect(Collectors.groupingBy(task -> task.getDeadline().toLocalDate()))
                .entrySet().stream()
                .map(entry -> new GroupedTaskResponseDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, UserTaskRequestDTO request) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task tapılmadı"));
        validatePermission(task, "UPDATE");

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskByAdmin(Long id, AdminTaskRequestDTO request) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task tapılmadı"));
        if (request.getAssigneeId() != null) {
            task.setAssignee(userRepository.findById(request.getAssigneeId()).orElseThrow());
        }
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task tapılmadı"));
        validatePermission(task, "CHANGE_STATUS");

        if (newStatus == TaskStatus.COMPLETED) {
            if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDateTime.now())) {
                try {
                    penaltyService.applyDeadlineMissedPenalty(taskId);
                } catch (RuntimeException e) {
                    if (!e.getMessage().contains("artıq deadline cəriməsi mövcuddur")) {
                        throw e;
                    }
                }
                throw new RuntimeException("Deadline keçib! Cərimə tətbiq olundu. Admin cəriməni bağışlayana qədər task tamamlana bilmez.");
            }
        }

        task.setStatus(newStatus);
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public String deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task tapılmadı"));
        validatePermission(task, "DELETE");
        task.setDeleted(true);
        taskRepository.save(task);
        return "Task silindi.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllActiveTasks() {
        if (isAdmin()) return taskRepository.findByDeletedFalse().stream().map(this::convertToResponse).collect(Collectors.toList());
        return taskRepository.findTasksByUserId(getCurrentUser().getId()).stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // --- YENİ ƏLAVƏ OLUNAN TƏHLÜKƏSİZ VƏ DİNAMİK FİLTR METODU ---
    @Override
    @Transactional
    public List<TaskResponseDTO> getAllTasksWithAdvancedFilters(String role, TaskStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        List<Task> tasks;

        String databaseRoleName = null;
        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("ALL")) {
            databaseRoleName = role.startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
        }

        // Giriş edən şəxsə görə repodan süzülmüş datanı çəkirik
        if (isAdmin()) {
            tasks = taskRepository.findTasksByAdminFilters(databaseRoleName, status, startDate, endDate);
        } else {
            tasks = taskRepository.findTasksByUserIdAndFilters(getCurrentUser().getId(), status, startDate, endDate);
        }

        // STATUS YOXLAMA VƏ AVTOMATİK YENİLƏMƏ BLOKU
        LocalDateTime now = LocalDateTime.now();
        for (Task task : tasks) {
            if (task.getDeadline() != null && task.getDeadline().isBefore(now)
                    && task.getStatus() != TaskStatus.COMPLETED
                    && task.getStatus() != TaskStatus.CANCELLED
                    && task.getStatus() != TaskStatus.OVERDUE) {

                task.setStatus(TaskStatus.OVERDUE);
                taskRepository.save(task);
            }
        }

        return tasks.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // Helper metodlar
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }
    private boolean isCreator(Task task) { return task.getCreator().getId().equals(getCurrentUser().getId()); }
    private boolean isAssignee(Task task) { return task.getAssignee() != null && task.getAssignee().getId().equals(getCurrentUser().getId()); }
    private User getCurrentUser() { String email = SecurityContextHolder.getContext().getAuthentication().getName(); return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı")); }

    private TaskResponseDTO convertToResponse(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDeadline(task.getDeadline());

        if (task.getCreator() != null) {
            dto.setCreatorName(task.getCreator().getFullName());
        } else {
            dto.setCreatorName("Sistem");
        }

        if (task.getAssignee() != null) {
            dto.setAssigneeName(task.getAssignee().getFullName());
        } else {
            dto.setAssigneeName("Təyin edilməyib");
        }
        return dto;
    }
}