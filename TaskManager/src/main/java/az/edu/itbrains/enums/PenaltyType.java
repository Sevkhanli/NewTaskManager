package az.edu.itbrains.enums;

public enum PenaltyType {
    DEADLINE_MISSED,        // Deadline keçib, task tamamlanmayıb
    STATUS_NOT_COMPLETED,   // Task tamamlanıb amma status COMPLETED deyil
    FALSE_COMPLETION        // Yalançı tamamlama (proof olmadan)
}
