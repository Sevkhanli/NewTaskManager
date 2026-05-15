package az.edu.itbrains.repositories;

import az.edu.itbrains.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    // Email üçün (Optional istifadə etmək ən təhlükəsizidir)
    Optional<User> findByEmail(String email);

    // Email mövcudluğunu yoxlamaq üçün
    Boolean existsByEmail(String email);

    // Təsdiqlənməmiş istifadəçini tapmaq üçün
    Optional<User> findByEmailAndVerifiedFalse(String email);
}