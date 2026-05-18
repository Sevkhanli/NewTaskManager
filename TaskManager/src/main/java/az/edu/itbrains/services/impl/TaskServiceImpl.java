package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.AssignTaskToRoleRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.PenaltyStatus;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.models.Penalty;
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

        task.setId(null); // <--- BU SƏTRİ ƏLAVƏ ET (Çox vacibdir!)

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

        task.setId(null); // <--- BU SƏTRİ BURADA DA ƏLAVƏ ET

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
        // 1. Göndərilən rola sahib olan bütün userləri tapırıq
        List<User> usersInRole = userRepository.findByRoles_Name(request.getRoleName());

        if (usersInRole.isEmpty()) {
            throw new RuntimeException(request.getRoleName() + " roluna sahib heç bir istifadəçi tapılmadı!");
        }

        User adminUser = getCurrentUser(); // Taskı yaradan admin

        // 2. Hər bir istifadəçi üçün ayrıca task yaradıb yadda saxlayırıq
        for (User user : usersInRole) {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDeadline(request.getDeadline());

            task.setCreator(adminUser);
            task.setAssignee(user); // Bu task artıq bu spesifik userə məxsusdur
            task.setStatus(TaskStatus.PENDING);
            task.setCreatedAt(LocalDateTime.now());
            task.setDeleted(false);

            taskRepository.save(task);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupedTaskResponseDTO> getTasksGroupedByDate() {
        // 1. Mövcud metodla taskları çəkirik (Admin/User yoxlaması daxil)
        List<TaskResponseDTO> allTasks = getAllActiveTasks();

        // 2. Stream API ilə tarixlərə görə qruplaşdırırıq
        // Deadline null olanları "Tarixsiz" qrupuna yığa bilərik və ya filtr edə bilərik
        return allTasks.stream()
                .filter(task -> task.getDeadline() != null) // Deadline-ı olanlar
                .collect(Collectors.groupingBy(task -> task.getDeadline().toLocalDate()))
                .entrySet().stream()
                .map(entry -> new GroupedTaskResponseDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate())) // Tarixə görə sıralama
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
        // Admin üçün xüsusi yoxlamağa ehtiyac yoxdur, çünki validatePermission-da isAdmin() yoxdur.

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

        // COMPLETED statusuna keçərkən penalty yoxlanması
        if (newStatus == TaskStatus.COMPLETED) {
            // Deadline keçibsə və cərimə yoxdursa, cərimə tətbiq et
            if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDateTime.now())) {
                // Əvvəlcə cəriməni tətbiq et (rollback olmasın deyə ayrı transaction)
                try {
                    penaltyService.applyDeadlineMissedPenalty(taskId);
                } catch (RuntimeException e) {
                    if (!e.getMessage().contains("artıq deadline cəriməsi mövcuddur")) {
                        throw e;
                    }
                    // Cərimə artıq varsa, davam et
                }

                // Cərimə tətbiq olundu, taskın tamamlanmasına icazə vermə
                throw new RuntimeException("Deadline keçib! Cərimə tətbiq olundu. Admin cəriməni bağışlayana qədər task tamamlana bilməz.");
            }
        }

        task.setStatus(newStatus);
        // Gələcəkdə burada StatusHistory-ni qeyd edəcəyik
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

        // Creator üçün null yoxlaması
        if (task.getCreator() != null) {
            dto.setCreatorName(task.getCreator().getFullName());
        } else {
            dto.setCreatorName("Sistem");
        }

        // Assignee üçün null yoxlaması
        if (task.getAssignee() != null) {
            dto.setAssigneeName(task.getAssignee().getFullName());
        } else {
            dto.setAssigneeName("Təyin edilməyib");
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllActiveTasks() {
        if (isAdmin()) return taskRepository.findByDeletedFalse().stream().map(this::convertToResponse).collect(Collectors.toList());
        return taskRepository.findTasksByUserId(getCurrentUser().getId()).stream().map(this::convertToResponse).collect(Collectors.toList());
    }
}