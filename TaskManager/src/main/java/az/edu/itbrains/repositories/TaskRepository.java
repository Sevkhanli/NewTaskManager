package az.edu.itbrains.repositories;

import az.edu.itbrains.models.Task;
import az.edu.itbrains.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByDeletedFalse();

    @Query("SELECT t FROM Task t WHERE t.deleted = false AND (t.creator.id = :userId OR t.assignee.id = :userId)")
    List<Task> findTasksByUserId(@Param("userId") Long userId);

    List<Task> findByAssigneeRolesName(String databaseRoleName);

    // --- ADMIN FİLTRİ (TAM DOĞRU VERSİYA) ---
    @Query("SELECT t FROM Task t LEFT JOIN t.assignee a LEFT JOIN a.roles r WHERE t.deleted = false " +
            "AND (:roleName IS NULL OR r.name = :roleName) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (COALESCE(:startDate, NULL) IS NULL OR t.deadline >= :startDate) " +
            "AND (COALESCE(:endDate, NULL) IS NULL OR t.deadline <= :endDate)")
    List<Task> findTasksByAdminFilters(
            @Param("roleName") String roleName,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // --- USER FİLTRİ (TAM DOĞRU VERSİYA) ---
    @Query("SELECT t FROM Task t WHERE t.deleted = false " +
            "AND (t.creator.id = :userId OR t.assignee.id = :userId) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (COALESCE(:startDate, NULL) IS NULL OR t.deadline >= :startDate) " +
            "AND (COALESCE(:endDate, NULL) IS NULL OR t.deadline <= :endDate)")
    List<Task> findTasksByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}