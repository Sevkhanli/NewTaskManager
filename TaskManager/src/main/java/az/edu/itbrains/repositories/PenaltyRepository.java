package az.edu.itbrains.repositories;

import az.edu.itbrains.enums.PenaltyStatus;
import az.edu.itbrains.enums.PenaltyType;
import az.edu.itbrains.models.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("SELECT p FROM Penalty p WHERE p.user.id = :userId")
    List<Penalty> findByUserId(@Param("userId") Long userId);

    List<Penalty> findByUserIdAndStatus(Long userId, PenaltyStatus status);

    List<Penalty> findByTaskId(Long taskId);

    List<Penalty> findByStatus(PenaltyStatus status);

    @Query("SELECT p FROM Penalty p WHERE p.task.id = :taskId AND p.penaltyType = :type AND p.status IN ('PENDING', 'DISPUTED')")
    Optional<Penalty> findActivePenaltyByTaskAndType(@Param("taskId") Long taskId, @Param("type") PenaltyType type);

    @Query("SELECT SUM(p.amount) FROM Penalty p WHERE p.user.id = :userId AND p.status = 'PENDING'")
    Double getTotalPendingAmountByUserId(@Param("userId") Long userId);

    boolean existsByTaskIdAndPenaltyType(Long taskId, PenaltyType penaltyType);
}
