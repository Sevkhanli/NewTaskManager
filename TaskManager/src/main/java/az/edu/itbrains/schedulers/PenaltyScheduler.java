package az.edu.itbrains.schedulers;

import az.edu.itbrains.services.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PenaltyScheduler {

    private final PenaltyService penaltyService;

    /**
     * Hər saat başı deadline keçmiş taskları yoxlayır və cərimə tətbiq edir
     * Cron: 0 0 * * * * = Hər saatın 0-cı dəqiqəsi, 0-cı saniyəsi
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkOverdueTasks() {
        log.info("Starting overdue tasks check...");
        try {
            penaltyService.checkOverdueTasksAndApplyPenalties();
            log.info("Overdue tasks check completed successfully");
        } catch (Exception e) {
            log.error("Error during overdue tasks check: {}", e.getMessage(), e);
        }
    }

    /**
     * Hər gün saat 00:00-da statusu tamamlanmamış taskları yoxlayır
     * Cron: 0 0 0 * * * = Hər gün gecə yarım
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkIncompleteStatusTasks() {
        log.info("Starting incomplete status tasks check...");
        try {
            penaltyService.checkTasksWithoutCompletedStatus();
            log.info("Incomplete status tasks check completed successfully");
        } catch (Exception e) {
            log.error("Error during incomplete status check: {}", e.getMessage(), e);
        }
    }
}
