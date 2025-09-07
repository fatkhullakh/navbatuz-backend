package uz.navbatuz.backend.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${app.mail.from}") private String from;

    public void sendHtml(String to, String subject, String html) {
        try {
            log.info("SES send start to={} subj={}", to, subject);
            var msg = mailSender.createMimeMessage();
            var h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
            log.info("SES send ok to={}", to);
        } catch (Exception e) {
            log.error("SES send FAILED to={} reason={}", to, e.getMessage(), e);
            throw new RuntimeException("EMAIL_SEND_FAILED", e);
        }
    }
}
