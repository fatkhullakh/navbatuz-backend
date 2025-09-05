package uz.navbatuz.backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${app.mail.from}") private String from;

    public void sendHtml(String to, String subject, String html) {
        try {
            var msg = mailSender.createMimeMessage();
            var h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("EMAIL_SEND_FAILED", e);
        }
    }
}
