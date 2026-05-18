package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.*;
import az.edu.itbrains.DTOs.response.AuthResponseDTO;
import az.edu.itbrains.DTOs.response.UserListDTO;
import az.edu.itbrains.exceptions.*;
import az.edu.itbrains.models.RevokedToken;
import az.edu.itbrains.models.Role;
import az.edu.itbrains.models.User;
import az.edu.itbrains.models.VerificationToken;
import az.edu.itbrains.repositories.RevokedTokenRepository;
import az.edu.itbrains.repositories.RoleRepository;
import az.edu.itbrains.repositories.UserRepository;
import az.edu.itbrains.repositories.VerificationTokenRepository;
import az.edu.itbrains.security.JwtService;
import az.edu.itbrains.services.MailService;
import az.edu.itbrains.services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerificationTokenRepository tokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            VerificationTokenRepository tokenRepository,
            RevokedTokenRepository revokedTokenRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<UserListDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserListDTO(user.getId(), user.getFullName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthResponseDTO createUserByAdmin(RegisterRequestDTO request) {
        // 1. Email yoxlanışı
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Bu email ilə istifadəçi artıq mövcuddur.");
        }

        // 2. Yeni istifadəçi obyekti yaradılır
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Admin tərəfindən təyin edilən şifrəni encode edirik
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. Rolların DINAMIK təyin edilməsi (DÜZƏLİŞ OLUNAN HİSSƏ)
        String targetedRole = (request.getRoleName() != null && !request.getRoleName().trim().isEmpty())
                ? request.getRoleName().trim()
                : "ROLE_USER"; // Əgər rol gəlməzsə, default olaraq USER qeyd etsin

        // Bazada bu adda rolun olub-olmadığını yoxlayırıq, yoxdursa avtomatik yaradırıq
        Role assignedRole = roleRepository.findByName(targetedRole)
                .orElseGet(() -> roleRepository.save(new Role(targetedRole)));

        // İstifadəçiyə seçilən rolu mənimsədirik
        user.getRoles().add(assignedRole);

        // 4. Admin yaratdığı üçün birbaşa təsdiqlənmiş (verified) edirik
        user.setVerified(true);

        userRepository.save(user);

        return new AuthResponseDTO(true, "İstifadəçi admin tərəfindən '" + targetedRole + "' rolu ilə uğurla yaradıldı.");
    }

//    @Override
//    @Transactional
//    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new InvalidCredentialsException("Şifrələr bir-biri ilə uyğun gəlmir.");
//        }
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new UserAlreadyExistsException("Bu email artıq qeydiyyatdan keçib.");
//        }
//
//        User user = new User();
//        user.setFullName(request.getFullName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        Role userRole = roleRepository.findByName("ROLE_USER")
//                .orElseGet(() -> {
//                    Role defaultRole = new Role("ROLE_USER");
//                    return roleRepository.save(defaultRole);
//                });
//
//        user.getRoles().add(userRole);
//        user.setVerified(false);
//
//        User savedUser = userRepository.save(user);
//        sendOrUpdateOtp(savedUser, false);
//
//        return new AuthResponseDTO(true, "Qeydiyyat uğurludur. Email təsdiqi tələb olunur.");
//    }

    @Override
    @Transactional
    public AuthResponseDTO verifyUser(VerifyRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        VerificationToken verificationToken = tokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidOtpException("Təsdiq kodu tapılmadı."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new OtpExpiredException("OTP kodunun vaxtı bitib.");
        }

        if (!request.getOtpCode().equals(verificationToken.getToken())) {
            throw new InvalidOtpException("Daxil etdiyiniz OTP kodu yanlışdır.");
        }

        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        return new AuthResponseDTO(true, "Email uğurla təsdiqləndi.");
    }

    @Override
    @Transactional
    public void resendOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        if (user.isVerified()) throw new UserAlreadyVerifiedException("İstifadəçi artıq təsdiqlənib.");

        sendOrUpdateOtp(user, false);
    }

    @Override
    @Transactional
    public AuthResponseDTO loginUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email və ya şifrə yanlışdır."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }

        if (!user.isVerified()) {
            sendOrUpdateOtp(user, false);
            return new AuthResponseDTO(false, "Email təsdiqi tələb olunur. Yeni OTP göndərildi.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
            return new AuthResponseDTO(true, "Giriş uğurludur.",
                    jwtService.generateToken(user), jwtService.generateRefreshToken(user));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        if (revokedTokenRepository.existsByToken(refreshToken)) {
            throw new InvalidTokenException("Bu Refresh Token ləğv edilib. Yenidən giriş edin.");
        }

        String email = jwtService.findUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Tapılmadı."));

        return new AuthResponseDTO(true, "Yeniləndi.",
                jwtService.generateToken(user), jwtService.generateRefreshToken(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        AuthResponseDTO resp = new AuthResponseDTO();
        resp.setSuccess(true);
        resp.setMessage("Profil məlumatları uğurlu.");
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        return resp;
    }

    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        sendOrUpdateOtp(user, true);
    }

    @Override
    @Transactional
    public AuthResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Şifrələr uyğun deyil.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        VerificationToken vt = tokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidOtpException("Kod tapılmadı."));

        if (!request.getOtpCode().equals(vt.getToken())) throw new InvalidOtpException("Kod yanlışdır.");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.delete(vt);

        return new AuthResponseDTO(true, "Şifrə uğurla yeniləndi.");
    }

    @Override
    @Transactional
    public void logout(String authHeader, String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidTokenException("Refresh Token tələb olunur.");
        }

        jwtService.findUsername(refreshToken);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            saveRevokedToken(authHeader.substring(7).trim());
        }
        saveRevokedToken(refreshToken.trim());
    }

    private void saveRevokedToken(String token) {
        if (!revokedTokenRepository.existsByToken(token)) {
            RevokedToken rt = new RevokedToken();
            rt.setToken(token);
            rt.setRevokedAt(LocalDateTime.now());
            revokedTokenRepository.save(rt);
        }
    }

    @Override
    @Transactional
    public void resendForgotPasswordOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        sendOrUpdateOtp(user, true);
    }

    private void sendOrUpdateOtp(User user, boolean isResetPassword) {
        VerificationToken token = tokenRepository.findByUser(user)
                .orElseGet(() -> {
                    VerificationToken newToken = new VerificationToken();
                    newToken.setUser(user);
                    return newToken;
                });

        String otpCode = generateOtp();
        token.setToken(otpCode);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(token);

        // Birbaşa MailService çağırışı
        String subject = isResetPassword ? "IT Brains - Şifrə Sıfırlama Kodu" : "IT Brains - Email Təsdiqi Kodu (OTP)";
        String content = isResetPassword ? "Şifrənizi yeniləmək üçün kod:" : "Qeydiyyatınızı tamamlamaq üçün kod:";

        mailService.sendOtpEmail(user.getEmail(), otpCode, subject, content);

        System.out.println("-----------------------------------------");
        System.out.println("DEBUG: GÖNDƏRİLƏN OTP KODU: " + otpCode);
        System.out.println("-----------------------------------------");
    }

    private String generateOtp() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}