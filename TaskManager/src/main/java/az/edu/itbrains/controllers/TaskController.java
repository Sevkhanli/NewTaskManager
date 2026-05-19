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

    @Operation(summary = "Create a task for the current user")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createMyTask(@Valid @RequestBody UserTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.createMyTask(request));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a task as Admin")
    public ResponseEntity<TaskResponseDTO> createAdminTask(@Valid @RequestBody AdminTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.createTaskAsAdmin(request));
    }

    // --- OXUMA (BİRLƏŞDİRİLMİŞ DİNAMİK SİYAHI) ---

    @Operation(summary = "Get all active tasks with advanced filters")
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

        List<TaskResponseDTO> response = taskService.getAllTasksWithAdvancedFilters(role, status, start, end);
        return ResponseEntity.ok(response);
    }

    // --- OXUMA (YENİLƏNMİŞ DİNAMİK QRUPLAŞDIRMA / FOLDER REJİMİ) ---
    @Operation(summary = "Get tasks grouped by date with advanced filters")
    @GetMapping("/grouped")
    public ResponseEntity<List<GroupedTaskResponseDTO>> getGroupedTasks(
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

        List<GroupedTaskResponseDTO> response = taskService.getTasksGroupedByDate(role, status, start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // --- YENİLƏNMƏ (UPDATE) ---

    @PutMapping("/{id}")
    @Operation(summary = "Update user task")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @Valid @RequestBody UserTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update task as Admin")
    public ResponseEntity<TaskResponseDTO> updateTaskByAdmin(@PathVariable Long id, @Valid @RequestBody AdminTaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTaskByAdmin(id, request));
    }

    @PostMapping("/admin/assign-to-role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a task for all users in a specific role")
    public ResponseEntity<String> createTaskForRole(@Valid @RequestBody AssignTaskToRoleRequestDTO request) {
        taskService.createTaskForRole(request);
        return ResponseEntity.ok("Task qeyd olunan roldakı bütün istifadəçilərə uğurla təyin edildi.");
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status")
    public ResponseEntity<TaskResponseDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus newStatus,
            @RequestParam String reason) {
        return ResponseEntity.ok(taskService.changeStatus(id, newStatus, reason));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.deleteTask(id));
    }
}