package az.edu.itbrains.controllers;

import az.edu.itbrains.DTOs.request.AdminTaskRequestDTO;
import az.edu.itbrains.DTOs.request.AssignTaskToRoleRequestDTO;
import az.edu.itbrains.DTOs.request.UserTaskRequestDTO;
import az.edu.itbrains.DTOs.response.GroupedTaskResponseDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks by users and admins")
public class TaskController {

    private final TaskService taskService;

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

    // --- OXUMA (BİRLƏŞDİRİLMİŞ VƏ OPTİMALLAŞDIRILMIŞ METOD) ---

    @Operation(summary = "Get all active tasks with advanced filters", description = "Retrieves tasks filtered by role, status, and deadline date range. If no filters provided, fetches all active tasks.")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllActiveTasks(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null && !startDate.isEmpty()) {
            start = java.time.LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // Bütün filtrasiya və təhlükəsizlik məntiqi Service daxilində idarə olunur
        List<TaskResponseDTO> response = taskService.getAllTasksWithAdvancedFilters(role, status, start, end);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tasks grouped by date", description = "Retrieves tasks organized into date folders (grouping by deadline).")
    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedTaskResponseDTO>> getGroupedTasks() {
        return ResponseEntity.ok(taskService.getTasksGroupedByDate());
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