package az.edu.itbrains.controllers;

import az.edu.itbrains.DTOs.request.PenaltyWaiveRequestDTO;
import az.edu.itbrains.DTOs.request.TaskCompletionRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyResponseDTO;
import az.edu.itbrains.DTOs.response.UserPenaltySummaryDTO;
import az.edu.itbrains.services.PenaltyService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
@Tag(name = "Penalty Management", description = "Endpoints for managing user penalties and task completions")
public class PenaltyController {

    private final PenaltyService penaltyService;

    // ============ USER ENDPOINTLƏRİ ============

    @GetMapping("/my-penalties")
    @Operation(summary = "Get current user's penalties", description = "Returns a list of all penalties associated with the authenticated user")
    public ResponseEntity<List<PenaltyResponseDTO>> getMyPenalties() {
        return ResponseEntity.ok(penaltyService.getCurrentUserPenalties());
    }

    @GetMapping("/my-summary")
    @Operation(summary = "Get current user's penalty summary", description = "Returns total count and sum of pending penalties for the authenticated user")
    public ResponseEntity<UserPenaltySummaryDTO> getMyPenaltySummary() {
        return ResponseEntity.ok(penaltyService.getCurrentUserPenaltySummary());
    }

    @PostMapping("/complete-task")
    @Operation(summary = "Complete a task", description = "Validates task completion and checks for potential late submission penalties")
    public ResponseEntity<String> completeTask(@Valid @RequestBody TaskCompletionRequestDTO request) {
        penaltyService.validateTaskCompletion(request.getTaskId(), request);
        return ResponseEntity.ok("Task uğurla tamamlandı");
    }

    // ============ ADMIN ENDPOINTLƏRİ ============

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pending penalties", description = "Admin only: Returns a list of all penalties that are not yet paid or waived")
    public ResponseEntity<List<PenaltyResponseDTO>> getAllPendingPenalties() {
        return ResponseEntity.ok(penaltyService.getAllPendingPenalties());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get penalties by user ID", description = "Admin only: Returns all penalties for a specific user")
    public ResponseEntity<List<PenaltyResponseDTO>> getUserPenalties(@PathVariable Long userId) {
        return ResponseEntity.ok(penaltyService.getUserPenalties(userId));
    }

    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user penalty summary by ID", description = "Admin only: Returns penalty summary for a specific user")
    public ResponseEntity<UserPenaltySummaryDTO> getUserPenaltySummary(@PathVariable Long userId) {
        return ResponseEntity.ok(penaltyService.getUserPenaltySummary(userId));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get penalties by task ID", description = "Admin only: Returns all penalties associated with a specific task")
    public ResponseEntity<List<PenaltyResponseDTO>> getTaskPenalties(@PathVariable Long taskId) {
        return ResponseEntity.ok(penaltyService.getTaskPenalties(taskId));
    }

    @PostMapping("/waive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Waive a penalty", description = "Admin only: Forgives a penalty with a specific reason")
    public ResponseEntity<PenaltyResponseDTO> waivePenalty(@Valid @RequestBody PenaltyWaiveRequestDTO request) {
        return ResponseEntity.ok(penaltyService.waivePenalty(request));
    }

    @PostMapping("/{penaltyId}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark penalty as paid", description = "Admin only: Updates the status of a penalty to PAID")
    public ResponseEntity<PenaltyResponseDTO> markPenaltyAsPaid(@PathVariable Long penaltyId) {
        return ResponseEntity.ok(penaltyService.markPenaltyAsPaid(penaltyId));
    }
}
