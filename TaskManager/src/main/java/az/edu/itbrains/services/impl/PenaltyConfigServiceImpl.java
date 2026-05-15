package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.PenaltyConfigRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyConfigResponseDTO;
import az.edu.itbrains.models.PenaltyConfig;
import az.edu.itbrains.models.User;
import az.edu.itbrains.repositories.PenaltyConfigRepository;
import az.edu.itbrains.repositories.UserRepository;
import az.edu.itbrains.services.PenaltyConfigService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PenaltyConfigServiceImpl implements PenaltyConfigService {

    private final PenaltyConfigRepository penaltyConfigRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public PenaltyConfigResponseDTO getActiveConfig() {
        PenaltyConfig config = penaltyConfigRepository.findByActiveTrue()
                .orElseGet(this::createDefaultConfig);
        return convertToResponse(config);
    }

    @Override
    @Transactional
    public PenaltyConfigResponseDTO createOrUpdateConfig(PenaltyConfigRequestDTO request) {
        // Əvvəlki aktiv config-i deaktiv et
        penaltyConfigRepository.findByActiveTrue().ifPresent(oldConfig -> {
            oldConfig.setActive(false);
            penaltyConfigRepository.save(oldConfig);
        });

        // Yeni config yarat
        PenaltyConfig config = new PenaltyConfig();
        config.setDeadlineMissedAmount(request.getDeadlineMissedAmount());
        config.setStatusNotCompletedAmount(request.getStatusNotCompletedAmount());
        config.setFalseCompletionAmount(request.getFalseCompletionAmount());
        config.setCurrency(request.getCurrency() != null ? request.getCurrency() : "AZN");
        config.setActive(true);
        config.setUpdatedBy(getCurrentUser());

        PenaltyConfig saved = penaltyConfigRepository.save(config);
        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PenaltyConfigResponseDTO getConfigById(Long id) {
        PenaltyConfig config = penaltyConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cərimə konfiqurasiyası tapılmadı"));
        return convertToResponse(config);
    }

    private PenaltyConfig createDefaultConfig() {
        // Defolt dəyərlər: 5-10 AZN arası
        PenaltyConfig defaultConfig = new PenaltyConfig();
        defaultConfig.setDeadlineMissedAmount(5.0);
        defaultConfig.setStatusNotCompletedAmount(10.0);
        defaultConfig.setFalseCompletionAmount(10.0);
        defaultConfig.setCurrency("AZN");
        defaultConfig.setActive(true);
        return penaltyConfigRepository.save(defaultConfig);
    }

    private PenaltyConfigResponseDTO convertToResponse(PenaltyConfig config) {
        PenaltyConfigResponseDTO dto = modelMapper.map(config, PenaltyConfigResponseDTO.class);
        if (config.getUpdatedBy() != null) {
            dto.setUpdatedByName(config.getUpdatedBy().getFullName());
        }
        return dto;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
