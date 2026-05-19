package az.edu.itbrains.controllers;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.AssignTaskToRoleRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.models.Task;
import az.edu.itbrains.repositories.TaskRepository;
import az.edu.itbrains.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks by users and admins")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    // --- YARADILMA ---

    @Operation(summary = "Create a task for the current user", description = "Allows a user to create a task assigned to themselves.")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createMyTask(@Valid @RequestBody UserTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.createMyTask(request));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a task as Admin", description = "Allows an admin to create a task and assign it to any user.")
    public ResponseEntity<TaskResponseDTO> createAdminTask(@Valid @RequestBody AdminTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.createTaskAsAdmin(request));
    }

    // --- OXUMA ---

    @Operation(summary = "Get tasks grouped by date", description = "Retrieves tasks organized into date folders (grouping by deadline).")
    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedTaskResponseDTO>> getGroupedTasks() {
        return ResponseEntity.ok(taskService.getTasksGroupedByDate());
    }

    // HƏLL YOLU: Köhnə metodu Parametr qəbul edəcək şəkildə genişləndirdik!
    @Operation(summary = "Get all active tasks (Flat list) with optional role filtering", description = "Retrieves tasks. If role parameter is provided, filters tasks by assignee's role.")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllActiveTasks(@RequestParam(required = false) String role) {
        List<Task> tasks;

        // Əgər front-end-den rol filtri gəlibsə və 'ALL' deyilsə
        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("ALL")) {
            String databaseRoleName = role.startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();

            // Repository-dəki düzgün metodu çağırırıq
            tasks = taskRepository.findByAssigneeRolesName(databaseRoleName);
        } else {
            // Rol gəlməyibsə onsuz da var olan köhnə məntiqi (bütün aktiv taskları) işlədirik
            tasks = taskRepository.findAll().stream()
                    .filter(task -> !task.isDeleted())
                    .toList();
        }

        // Sənin yazdığın DTO map məntiqi
        List<TaskResponseDTO> response = tasks.stream()
                .map(task -> modelMapper.map(task, TaskResponseDTO.class))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves details of a specific task by its unique identifier.")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // --- YENİLƏNMƏ (UPDATE) ---

    @PutMapping("/{id}")
    @Operation(summary = "Update user task", description = "Allows a user to update the details of their own task.")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @Valid @RequestBody UserTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update task as Admin", description = "Allows an admin to update any task, including changing the assignee.")
    public ResponseEntity<TaskResponseDTO> updateTaskByAdmin(@PathVariable Long id, @Valid @RequestBody AdminTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTaskByAdmin(id, request));
    }

    @PostMapping("/admin/assign-to-role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a task for all users in a specific role", description = "Allows an admin to bulk-assign a task to all users belonging to a specific role.")
    public ResponseEntity<String> createTaskForRole(@Valid @RequestBody AssignTaskToRoleRequestDTO request) {
        taskService.createTaskForRole(request);
        return ResponseEntity.ok("Task qeyd olunan roldakı bütün istifadəçilərə uğurla təyin edildi.");
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status", description = "Updates the status of a task and records the reason for the change.")
    public ResponseEntity<TaskResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus newStatus,
            @RequestParam String reason) {

        return ResponseEntity.ok(taskService.changeStatus(id, newStatus, reason));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Soft deletes a task from the system.")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.deleteTask(id));
    }
}