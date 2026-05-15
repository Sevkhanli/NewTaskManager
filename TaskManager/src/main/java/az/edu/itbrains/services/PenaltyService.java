package az.edu.itbrains.services;

import az.edu.itbrains.DTOs.request.PenaltyWaiveRequestDTO;
import az.edu.itbrains.DTOs.request.TaskCompletionRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyResponseDTO;
import az.edu.itbrains.DTOs.response.UserPenaltySummaryDTO;

import java.util.List;

public interface PenaltyService {

    // Cərimə əməliyyatları
    PenaltyResponseDTO applyDeadlineMissedPenalty(Long taskId);
    PenaltyResponseDTO applyStatusNotCompletedPenalty(Long taskId);
    PenaltyResponseDTO applyFalseCompletionPenalty(Long taskId);

    // Task tamamlama validasiyası
    void validateTaskCompletion(Long taskId, TaskCompletionRequestDTO request);

    // Cərimə siyahıları
    List<PenaltyResponseDTO> getUserPenalties(Long userId);
    List<PenaltyResponseDTO> getCurrentUserPenalties();
    List<PenaltyResponseDTO> getTaskPenalties(Long taskId);
    List<PenaltyResponseDTO> getAllPendingPenalties();

    // User xülasəsi
    UserPenaltySummaryDTO getCurrentUserPenaltySummary();
    UserPenaltySummaryDTO getUserPenaltySummary(Long userId);

    // Cərimə idarəetmə (Admin)
    PenaltyResponseDTO waivePenalty(PenaltyWaiveRequestDTO request);
    PenaltyResponseDTO markPenaltyAsPaid(Long penaltyId);

    // Schedule işləri
    void checkOverdueTasksAndApplyPenalties();
    void checkTasksWithoutCompletedStatus();
}
