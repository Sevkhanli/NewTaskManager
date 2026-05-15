package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.PenaltyWaiveRequestDTO;
import az.edu.itbrains.DTOs.request.TaskCompletionRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyResponseDTO;
import az.edu.itbrains.DTOs.response.UserPenaltySummaryDTO;
import az.edu.itbrains.enums.PenaltyStatus;
import az.edu.itbrains.enums.PenaltyType;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.models.Penalty;
import az.edu.itbrains.models.PenaltyConfig;
import az.edu.itbrains.models.Task;
import az.edu.itbrains.models.User;
import az.edu.itbrains.repositories.PenaltyConfigRepository;
import az.edu.itbrains.repositories.PenaltyRepository;
import az.edu.itbrains.repositories.TaskRepository;
import az.edu.itbrains.repositories.UserRepository;
import az.edu.itbrains.services.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final PenaltyConfigRepository penaltyConfigRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // ============ CƏRİMƏ TƏTBİQ ETMƏ ============

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PenaltyResponseDTO applyDeadlineMissedPenalty(Long taskId) {
        Task task = getTaskById(taskId);
        
        // Cərimə kimə tətbiq olunacaq? Assignee varsa ona, yoxsa creator-a
        User penaltyUser = (task.getAssignee() != null) ? task.getAssignee() : task.getCreator();

        // Əgər artıq bu tipdə cərimə varsa, yenisini yaratma
        if (penaltyRepository.existsByTaskIdAndPenaltyType(taskId, PenaltyType.DEADLINE_MISSED)) {
            throw new RuntimeException("Bu task üçün artıq deadline cəriməsi mövcuddur");
        }

        PenaltyConfig config = getActiveConfig();

        // Gün sayını hesabla (info üçün)
        int daysOverdue = (int) ChronoUnit.DAYS.between(task.getDeadline(), LocalDateTime.now());
        if (daysOverdue < 0) daysOverdue = 0;

        // Sabit cərimə məbləği (gün sayından asılı olmayan)
        double amount = config.getDeadlineMissedAmount();

        Penalty penalty = Penalty.builder()
                .user(penaltyUser)
                .task(task)
                .penaltyType(PenaltyType.DEADLINE_MISSED)
                .amount(amount)
                .currency(config.getCurrency())
                .daysOverdue(daysOverdue)
                .description("Deadline keçib: " + daysOverdue + " gün gecikmə")
                .status(PenaltyStatus.PENDING)
                .build();

        Penalty saved = penaltyRepository.save(penalty);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PenaltyResponseDTO applyStatusNotCompletedPenalty(Long taskId) {
        Task task = getTaskById(taskId);
        User assignee = task.getAssignee();

        // Əgər artıq bu tipdə cərimə varsa, yenisini yaratma
        if (penaltyRepository.existsByTaskIdAndPenaltyType(taskId, PenaltyType.STATUS_NOT_COMPLETED)) {
            throw new RuntimeException("Bu task üçün artıq status cəriməsi mövcuddur");
        }

        PenaltyConfig config = getActiveConfig();

        Penalty penalty = Penalty.builder()
                .user(assignee)
                .task(task)
                .penaltyType(PenaltyType.STATUS_NOT_COMPLETED)
                .amount(config.getStatusNotCompletedAmount())
                .currency(config.getCurrency())
                .description("Task tamamlanıb amma status COMPLETED edilməyib")
                .status(PenaltyStatus.PENDING)
                .evidenceRequired(true)
                .build();

        return convertToResponse(penaltyRepository.save(penalty));
    }

    @Override
    @Transactional
    public PenaltyResponseDTO applyFalseCompletionPenalty(Long taskId) {
        Task task = getTaskById(taskId);
        User assignee = task.getAssignee();

        // Əgər artıq bu tipdə cərimə varsa, yenisini yaratma
        if (penaltyRepository.existsByTaskIdAndPenaltyType(taskId, PenaltyType.FALSE_COMPLETION)) {
            throw new RuntimeException("Bu task üçün artıq yalançı tamamlama cəriməsi mövcuddur");
        }

        PenaltyConfig config = getActiveConfig();

        Penalty penalty = Penalty.builder()
                .user(assignee)
                .task(task)
                .penaltyType(PenaltyType.FALSE_COMPLETION)
                .amount(config.getFalseCompletionAmount())
                .currency(config.getCurrency())
                .description("Yalançı tamamlama - proof tələb olunur")
                .status(PenaltyStatus.PENDING)
                .evidenceRequired(true)
                .build();

        return convertToResponse(penaltyRepository.save(penalty));
    }

    // ============ TASK TAMAMLAMA VALIDASİYASI ============

    @Override
    @Transactional
    public void validateTaskCompletion(Long taskId, TaskCompletionRequestDTO request) {
        Task task = getTaskById(taskId);

        // Yalnız assignee və ya creator tamamlaya bilər
        if (!isAssignee(task) && !isCreator(task) && !isAdmin()) {
            throw new RuntimeException("Bu taskı tamamlamağa icazəniz yoxdur");
        }

        // Deadline keçibsə və cərimə yoxdursa, cərimə tətbiq et
        if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDateTime.now())) {
            if (!penaltyRepository.existsByTaskIdAndPenaltyType(taskId, PenaltyType.DEADLINE_MISSED)) {
                applyDeadlineMissedPenalty(taskId);
                throw new RuntimeException("Deadline keçib! Cərimə tətbiq olundu. Adminə müraciət edin.");
            }
        }

        // Proof tələb olunursa yoxla
        if (request.getEvidenceLink() == null || request.getEvidenceLink().trim().isEmpty()) {
            // Yoxla ki, əvvəl false completion cəriməsi varmı
            Optional<Penalty> existingPenalty = penaltyRepository.findActivePenaltyByTaskAndType(
                    taskId, PenaltyType.FALSE_COMPLETION);

            if (existingPenalty.isPresent()) {
                throw new RuntimeException("Bu task üçün proof təqdim etməlisiniz. Cərimə mövcuddur.");
            }
        }

        // Taskı tamamla
        task.setStatus(TaskStatus.COMPLETED);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    // ============ SİYAHLAR VƏ XÜLASƏ ============

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getUserPenalties(Long userId) {
        List<Penalty> penalties = penaltyRepository.findByUserId(userId);
        return penalties.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getCurrentUserPenalties() {
        User currentUser = getCurrentUser();
        
        // Bütün cərimələri yoxla (debug üçün)
        List<Penalty> allPenalties = penaltyRepository.findAll();
        System.out.println("Bütün cərimələr: " + allPenalties.size());
        for (Penalty p : allPenalties) {
            System.out.println("  Cərimə ID: " + p.getId() + ", User ID: " + p.getUser().getId() + ", Tip: " + p.getPenaltyType());
        }
        
        return getUserPenalties(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getTaskPenalties(Long taskId) {
        return penaltyRepository.findByTaskId(taskId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getAllPendingPenalties() {
        return penaltyRepository.findByStatus(PenaltyStatus.PENDING).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserPenaltySummaryDTO getCurrentUserPenaltySummary() {
        User currentUser = getCurrentUser();
        return getUserPenaltySummary(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserPenaltySummaryDTO getUserPenaltySummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        List<Penalty> penalties = penaltyRepository.findByUserId(userId);

        double pendingAmount = penalties.stream()
                .filter(p -> p.getStatus() == PenaltyStatus.PENDING)
                .mapToDouble(Penalty::getAmount)
                .sum();

        long pendingCount = penalties.stream()
                .filter(p -> p.getStatus() == PenaltyStatus.PENDING)
                .count();

        long paidCount = penalties.stream()
                .filter(p -> p.getStatus() == PenaltyStatus.PAID)
                .count();

        return UserPenaltySummaryDTO.builder()
                .userId(userId)
                .userName(user.getFullName())
                .totalPendingAmount(pendingAmount)
                .totalPenalties((long) penalties.size())
                .pendingPenalties(pendingCount)
                .paidPenalties(paidCount)
                .currency(penalties.isEmpty() ? "AZN" : penalties.get(0).getCurrency())
                .build();
    }

    // ============ CƏRİMƏ IDARƏETMƏ (ADMIN) ============

    @Override
    @Transactional
    public PenaltyResponseDTO waivePenalty(PenaltyWaiveRequestDTO request) {
        if (!isAdmin()) {
            throw new RuntimeException("Cərimə bağışlamağa yalnız admin icazəlidir");
        }

        Penalty penalty = penaltyRepository.findById(request.getPenaltyId())
                .orElseThrow(() -> new RuntimeException("Cərimə tapılmadı"));

        penalty.setStatus(PenaltyStatus.WAIVED);
        penalty.setWaivedBy(getCurrentUser());
        penalty.setWaivedAt(LocalDateTime.now());
        penalty.setWaiveReason(request.getWaiveReason());

        return convertToResponse(penaltyRepository.save(penalty));
    }

    @Override
    @Transactional
    public PenaltyResponseDTO markPenaltyAsPaid(Long penaltyId) {
        if (!isAdmin()) {
            throw new RuntimeException("Cəriməni ödənilmiş etməyə yalnız admin icazəlidir");
        }

        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Cərimə tapılmadı. ID: " + penaltyId));

        // Əgər cərimə artıq ödənilibsə
        if (penalty.getStatus() == PenaltyStatus.PAID) {
            throw new RuntimeException("Cərimə artıq ödənilib. ID: " + penaltyId);
        }

        // Əgər cərimə artıq bağışlanıbsa
        if (penalty.getStatus() == PenaltyStatus.WAIVED) {
            throw new RuntimeException("Cərimə artıq bağışlanıb. ID: " + penaltyId);
        }

        penalty.setStatus(PenaltyStatus.PAID);
        penalty.setPaidAt(LocalDateTime.now());

        return convertToResponse(penaltyRepository.save(penalty));
    }

    // ============ SCHEDULER İŞLƏRİ ============

    @Override
    @Transactional
    public void checkOverdueTasksAndApplyPenalties() {
        // Bütün aktiv taskları yoxla
        List<Task> activeTasks = taskRepository.findByDeletedFalse();

        for (Task task : activeTasks) {
            // Deadline keçib və task tamamlanmayıbsa
            if (task.getDeadline() != null
                    && task.getDeadline().isBefore(LocalDateTime.now())
                    && task.getStatus() != TaskStatus.COMPLETED
                    && task.getStatus() != TaskStatus.CANCELLED) {

                // Əgər artıq cərimə yoxdursa, tətbiq et
                if (!penaltyRepository.existsByTaskIdAndPenaltyType(task.getId(), PenaltyType.DEADLINE_MISSED)) {
                    try {
                        applyDeadlineMissedPenalty(task.getId());
                        System.out.println("Cərimə tətbiq olundu - Task ID: " + task.getId());
                    } catch (Exception e) {
                        System.err.println("Cərimə tətbiq edilə bilmədi - Task ID: " + task.getId() + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void checkTasksWithoutCompletedStatus() {
        // Bu metod manual olaraq çağırılacaq - admin review üçün
        // Task tamamlanıbsa amma status COMPLETED deyilsə
    }

    // ============ HELPER METODLAR ============

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task tapılmadı"));
    }

    private PenaltyConfig getActiveConfig() {
        return penaltyConfigRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("Aktiv cərimə konfiqurasiyası tapılmadı"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isCreator(Task task) {
        return task.getCreator().getId().equals(getCurrentUser().getId());
    }

    private boolean isAssignee(Task task) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(getCurrentUser().getId());
    }

    private PenaltyResponseDTO convertToResponse(Penalty penalty) {
        PenaltyResponseDTO dto = modelMapper.map(penalty, PenaltyResponseDTO.class);
        dto.setTaskId(penalty.getTask().getId());
        dto.setTaskTitle(penalty.getTask().getTitle());
        dto.setUserId(penalty.getUser().getId());
        dto.setUserName(penalty.getUser().getFullName());
        return dto;
    }
}
