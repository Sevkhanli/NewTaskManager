package az.edu.itbrains.controllers;


import az.edu.itbrains.DTOs.request.*;
import az.edu.itbrains.DTOs.response.AuthResponseDTO;
import az.edu.itbrains.DTOs.response.UserListDTO;
import az.edu.itbrains.exceptions.InvalidTokenException;
import az.edu.itbrains.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "İstifadəçi qeydiyyatı, giriş və şifrə əməliyyatları")
public class AuthController {

    private final UserService userService;

//    @Operation(summary = "Yeni istifadəçi qeydiyyatı")
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
//    }

    @Operation(summary = "Admin tərəfindən yeni istifadəçi yaradılması")
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')") // YALNIZ ADMIN GİRƏ BİLƏR
    public ResponseEntity<AuthResponseDTO> createUser(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserByAdmin(request));
    }

    @Operation(summary = "Sistemə giriş")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @Operation(summary = "Bütün istifadəçilərin siyahısını gətir (Admin üçün)")
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Email təsdiqləmə (OTP)")
    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verify(@Valid @RequestBody VerifyRequestDTO request) {
        return ResponseEntity.ok(userService.verifyUser(request));
    }

    @Operation(summary = "OTP kodunu yenidən göndər")
    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDTO> resendOtp(@Valid @RequestBody ResendRequestDTO request) {
        userService.resendOtp(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Yeni OTP kodu email ünvanınıza göndərildi."));
    }

    @Operation(summary = "Refresh token vasitəsilə yeni access token al")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Refresh Token düzgün formatda deyil.");
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }



    @Operation(summary = "Şifrəni unutdum - sıfırlama kodu göndər")
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Şifrə sıfırlama kodu emailinizə göndərildi."));
    }

    @Operation(summary = "Yeni şifrəni təyin et")
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @Operation(summary = "Şifrə sıfırlama kodunu yenidən göndər")
    @PostMapping("/resend-forgot-password-otp")
    public ResponseEntity<AuthResponseDTO> resendForgotPasswordOtp(@Valid @RequestBody ResendRequestDTO request) {
        userService.resendForgotPasswordOtp(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Şifrə sıfırlama kodu yenidən email ünvanınıza göndərildi."));
    }

    @Operation(summary = "Sistemdən çıxış")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader,
            // required = true edirik ki, göndərilməyəndə 400 xətası versin
            @RequestHeader(value = "X-Refresh-Token", required = true) String refreshToken) {

        userService.logout(authHeader, refreshToken);
        return ResponseEntity.ok("Uğurla çıxış edildi.");
    }

    @Operation(summary = "Cari istifadəçi profil məlumatlarını və analizləri gətir")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getMyProfile() {
        // 1. Token-dən gələn email məlumatını götürürük
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "Sessiya tapılmadı. Zəhmət olmasa giriş edin."));
        }

        String email = auth.getName();

        // 2. BURADA DİQQƏT: userService.getUserProfile(email) çağırıldıqda
        // sənin o şəkildə atdığın (sətir 93-110) kod işə düşəcək.
        // Həmin kod isə həm user, həm də analiz datalarını bir yerdə qaytarır.
        return ResponseEntity.ok(userService.getUserProfile(email));
    }
    }
