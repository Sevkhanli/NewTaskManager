package az.edu.itbrains.services.impl;

import az.edu.itbrains.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String subject, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("sevxanli77@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);

        String body = String.format("""
            Hörmətli istifadəçi,

            %s

            OTP Kodu: %s

            Qeyd: Bu kod 5 dəqiqə ərzində etibarlıdır.

            Hörmətlə,
            IT Brains Komandası
            """, messageContent, otpCode);

        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("SUCCESS: " + subject + " kodu " + toEmail + " ünvanına uğurla göndərildi.");
        } catch (Exception e) {
            System.err.println("ERROR: Mail göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }
}