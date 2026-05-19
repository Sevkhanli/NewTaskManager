package az.edu.itbrains.services;


import az.edu.itbrains.DTOs.request.*;
import az.edu.itbrains.DTOs.response.AuthResponseDTO;
import az.edu.itbrains.DTOs.response.UserListDTO;
import az.edu.itbrains.models.User;

import java.util.List;

public interface UserService {

    User getCurrentUser();

//    AuthResponseDTO registerUser(RegisterRequestDTO request);

    // Mövcud metodların sonuna əlavə et
    List<UserListDTO> getAllUsers();
    AuthResponseDTO updateUserRoleByAdmin(Long userId, String newRoleName);
    AuthResponseDTO createUserByAdmin(RegisterRequestDTO request); // Admin tərəfindən yaradılma
    // Tokensiz təsdiqləmə.
    AuthResponseDTO verifyUser(VerifyRequestDTO request);
    List<UserListDTO> getAllUsersWithFilter(String role);
    // Tokensiz təkrar göndərmə.
    void resendOtp(ResendRequestDTO request);
    // Admin tərəfindən istifadəçi şifrəsinin birbaşa sıfırlanması
    AuthResponseDTO resetUserPasswordByAdmin(Long userId, AdminResetPasswordRequestDTO request);
    AuthResponseDTO loginUser(LoginRequestDTO request);

    AuthResponseDTO refreshToken(String refreshToken);


    AuthResponseDTO getUserProfile(String email);

    void forgotPassword(ForgotPasswordRequestDTO request);
    AuthResponseDTO resetPassword(ResetPasswordRequestDTO request);

    void logout(String authHeader, String refreshToken);

    void resendForgotPasswordOtp(ResendRequestDTO request); // Yeni əlavə edildi
}
