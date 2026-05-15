package az.edu.itbrains.repositories;

import az.edu.itbrains.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Bu metod Spring-ə deyir ki:
    // "Get bazadan bütün taskları, amma yalnız deleted sahəsi false olanları gətir."
    List<Task> findByDeletedFalse();

    @Query("SELECT t FROM Task t WHERE t.deleted = false AND (t.creator.id = :userId OR t.assignee.id = :userId)")
    List<Task> findTasksByUserId(@Param("userId") Long userId);
}