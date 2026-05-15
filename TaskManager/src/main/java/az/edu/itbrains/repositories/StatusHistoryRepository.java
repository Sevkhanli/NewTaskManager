package az.edu.itbrains.repositories;

import az.edu.itbrains.models.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
}