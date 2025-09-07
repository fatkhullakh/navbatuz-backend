// src/main/java/uz/navbatuz/backend/auth/service/AuthService.java
package uz.navbatuz.backend.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.auth.dto.*;
import uz.navbatuz.backend.auth.model.PasswordResetToken;
import uz.navbatuz.backend.auth.repository.PasswordResetTokenRepository;
import uz.navbatuz.backend.common.*;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MessageService messageService;
    private final CustomerRepository customerRepository;
    private final PasswordResetTokenRepository resetRepo;
    private final EmailService emailService;

    @Value("${app.public-url:${app.publicUrl:https://birzum.app}}")
    private String publicUrl;

    private static final SecureRandom RNG = new SecureRandom();
    private static String url(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    private static String safe(String s){ return s == null ? "" : s; }

    private static String generateTempPassword(int len) {
        // no ambiguous chars; includes digits and a couple of symbols
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(RNG.nextInt(alphabet.length())));
        return sb.toString();
    }

    @Transactional
    public WorkerRegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use");
        }

        Language lang = request.getLanguage() != null ? request.getLanguage() : Language.RU;
        String country = (request.getCountry() != null && !request.getCountry().isBlank())
                ? request.getCountry().trim().toUpperCase() : "UZ";

        // If password not supplied, generate a temporary one
        final boolean generated = (request.getPassword() == null || request.getPassword().isBlank());
        final String clearPassword = generated ? generateTempPassword(12) : request.getPassword();

        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .createdAt(LocalDateTime.now())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(clearPassword))
                .isActive(true)
                .language(lang)
                .country(country)
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        // Create Customer only for CUSTOMER role (same as before)
        if (request.getRole() == Role.CUSTOMER && !customerRepository.existsById(user.getId())) {
            var builder = Customer.builder()
                    .user(user)
                    .favouriteShops(new ArrayList<>());

            var loc = request.getCustomerLocation();
            if (loc != null) {
                // normalize ISO-2 (prefer payload.countryIso2, else registration country)
                String iso2 = (loc.getCountryIso2() != null && !loc.getCountryIso2().isBlank())
                        ? loc.getCountryIso2().trim().toUpperCase()
                        : (user.getCountry() != null ? user.getCountry().trim().toUpperCase() : "UZ");

                builder
                        .defaultCenter(uz.navbatuz.backend.common.geo.Geo.point(loc.getLat(), loc.getLon()))
                        .countryIso2(iso2)
                        .city(safe(loc.getCity()))
                        .district(safe(loc.getDistrict()));
            } else {
                // fallback: at least persist ISO-2 from registration if present
                String iso2 = (user.getCountry() != null && !user.getCountry().isBlank())
                        ? user.getCountry().trim().toUpperCase()
                        : null;
                builder.countryIso2(iso2);
            }

            customerRepository.save(builder.build());
        }

        // Localized success (optional)
        Locale locale = switch (user.getLanguage()) {
            case UZ -> new Locale("uz");
            case RU -> new Locale("ru");
            default -> Locale.ENGLISH;
        };
        String message = messageService.get("user.created.success", locale);
        System.out.println("Localized message: " + message);

        // Send invite email when appropriate (WORKER / RECEPTIONIST)
        try {
            if (user.getRole() == Role.WORKER || user.getRole() == Role.RECEPTIONIST) {
                // RU template
                String subject = "Ваш аккаунт Birzum создан";
                String loginUrl = publicUrl; // you can deep-link to app login
                String roleLabel = (user.getRole() != null) ? user.getRole().name() : "USER";

                String html = """
                <div style="font-family:Arial,sans-serif;font-size:14px;line-height:1.55">
                  <p>Здравствуйте, %s!</p>
                  <p>Для вас создан учетный запись в <b>Birzum</b> (роль: <b>%s</b>).</p>
                  <p><b>Логин (email):</b> %s<br/>
                  <b>Временный пароль:</b> %s</p>
                  <p>Пожалуйста, войдите и смените пароль при первом входе.</p>
                  <p><a href="%s" style="color:#2563EB;text-decoration:none">Открыть Birzum</a></p>
                  <p style="color:#6b7280">Если вы не ожидали это письмо, просто игнорируйте его.</p>
                </div>
                """.formatted(
                        safe(user.getName()),
                        roleLabel,
                        user.getEmail(),
                        clearPassword,
                        loginUrl
                );

                emailService.sendHtml(user.getEmail(), subject, html);
                log.info("Invite email sent to {}", user.getEmail());
            }
        } catch (Exception e) {
            log.warn("Failed to send invite email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Return token (unchanged)
        // If your mobile app doesn't need a token here, you could return only IDs.
        String token = jwtService.generateToken(user);
        return new WorkerRegisterResponse(token, user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

        if (user.getDeletedAt() != null || !user.isActive()) {
            throw new BadCredentialsException("Account disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Wrong password");
        }
        return new AuthResponse(jwtService.generateToken(user));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        var email = req.email().trim().toLowerCase();
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("Forgot password for non-existent email (suppressed): {}", email);
            return;
        }

        User user = userOpt.get();

        if (user.getDeletedAt() != null || !user.isActive()) {
            log.info("Forgot password for deleted/inactive account (suppressed): {}", email);
            return; // no hint
        }
        resetRepo.deleteByUserAndExpiresAtBefore(user, LocalDateTime.now());

        String code = String.format("%06d", RNG.nextInt(1_000_000));
        var token = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        resetRepo.save(token);

        String link = publicUrl + "/reset?email=" + url(user.getEmail()) + "&code=" + code;
        String html = """
        <div style="font-family:Arial,sans-serif;font-size:14px">
          <p>Код для сброса пароля (действителен 15 минут):</p>
          <p style="font-size:22px;font-weight:bold;letter-spacing:3px">%s</p>
          <p>Или перейдите по ссылке: <a href="%s">%s</a></p>
          <p>Если вы не запрашивали сброс — просто игнорируйте это письмо.</p>
        </div>
        """.formatted(code, link, link);

        emailService.sendHtml(user.getEmail(), "Сброс пароля Birzum", html);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        var email = req.email().trim().toLowerCase();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or code"));

        if (!user.isActive() || user.getDeletedAt() != null) {
            throw new RuntimeException("Account deleted/disabled");
        }

        var now = LocalDateTime.now();
        var tokens = resetRepo.findByUserAndUsedFalseOrderByExpiresAtDesc(user);
        var match = tokens.stream()
                .filter(t -> t.getExpiresAt().isAfter(now))
                .filter(t -> passwordEncoder.matches(req.code(), t.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid email or code"));

        tokens.forEach(t -> t.setUsed(true));
        resetRepo.saveAll(tokens);

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        log.info("Password reset OK for user {}", user.getId());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}
