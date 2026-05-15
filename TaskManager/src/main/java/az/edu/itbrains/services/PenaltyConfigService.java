package az.edu.itbrains.services;

import az.edu.itbrains.DTOs.request.PenaltyConfigRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyConfigResponseDTO;

public interface PenaltyConfigService {
    PenaltyConfigResponseDTO getActiveConfig();
    PenaltyConfigResponseDTO createOrUpdateConfig(PenaltyConfigRequestDTO request);
    PenaltyConfigResponseDTO getConfigById(Long id);
}
