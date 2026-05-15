package az.edu.itbrains.config;

import az.edu.itbrains.models.Role;
import az.edu.itbrains.models.User;
import az.edu.itbrains.repositories.RoleRepository;
import az.edu.itbrains.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Rolları yarat (əgər yoxdursa)
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role userRole = createRoleIfNotFound("ROLE_USER");

        // 2. Super Admin-i yarat (əgər yoxdursa)
        String adminEmail = "admin@itbrains.edu.az";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setFullName("Super Admin");
            admin.setEmail(adminEmail);
            // Şifrəni bura daxil et (məsələn: Admin123!)
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setVerified(true); // Super admin birbaşa təsdiqlənmiş olur
            admin.setRoles(Set.of(adminRole, userRole)); // Həm admin, həm user rolu veririk

            userRepository.save(admin);

            System.out.println("-----------------------------------------");
            System.out.println("SİSTEM: İlk Super Admin yaradıldı!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Şifrə: Admin123!");
            System.out.println("-----------------------------------------");
        }
    }

    private Role createRoleIfNotFound(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}