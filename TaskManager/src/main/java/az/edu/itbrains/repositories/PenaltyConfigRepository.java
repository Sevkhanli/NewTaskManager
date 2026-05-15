package az.edu.itbrains.repositories;

import az.edu.itbrains.models.PenaltyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PenaltyConfigRepository extends JpaRepository<PenaltyConfig, Long> {
    Optional<PenaltyConfig> findByActiveTrue();
}
