package uz.navbatuz.backend.auth.service;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.auth.dto.*;
import uz.navbatuz.backend.auth.model.PasswordResetToken;
import uz.navbatuz.backend.auth.repository.PasswordResetTokenRepository;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.MessageService;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.auth.service.JwtService;
import uz.navbatuz.backend.common.EmailService;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

import static uz.navbatuz.backend.common.Language.RU;
import static uz.navbatuz.backend.common.Language.UZ;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MessageService messageService;
    private final CustomerRepository customerRepository;
    private final PasswordResetTokenRepository resetRepo;
    private static final SecureRandom RNG = new SecureRandom();
    private final EmailService emailService;
    @Value("${app.publicUrl}") private String publicUrl;

    /*
    Converts incoming request into a User object.
    Uses passwordEncoder.encode() to hash the password (never store raw passwords).
    Sets isActive = true.
     */
    public WorkerRegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use");
        }

        // Safe defaults to avoid NPEs
        Language lang = request.getLanguage() != null ? request.getLanguage() : Language.RU;
        String country = (request.getCountry() != null && !request.getCountry().isBlank())
                ? request.getCountry().trim().toUpperCase() : "UZ";

        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .createdAt(LocalDateTime.now())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .language(lang)
                .country(country)
                .role(request.getRole())
                .build();

        user = userRepository.save(user);// save & attach to persistence context

        // Step 2: Create Customer only if role is CUSTOMER and it doesn't exist
        if (request.getRole() == Role.CUSTOMER && !customerRepository.existsById(user.getId())) {
            Customer customer = Customer.builder()
                    .user(user)
                    .favouriteShops(new ArrayList<>())
                    .build();
            customerRepository.save(customer);
        }



        // Step 3: Localization
        Locale locale = switch (user.getLanguage()) {
            case UZ -> new Locale("uz");
            case RU -> new Locale("ru");
            default -> Locale.ENGLISH;
        };
        String message = messageService.get("user.created.success", locale);
        System.out.println("Localized message: " + message);

        // Step 4: Return token
        String token = jwtService.generateToken(user);
        return new WorkerRegisterResponse(token, user.getId());
    }


    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Wrong password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    private String genCode6() {
        // 6-digit numeric, zero-padded
        int n = RNG.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        var userOpt = userRepository.findByEmail(req.email().trim().toLowerCase());
        if (userOpt.isEmpty()) return; // 204 always

        var user = userOpt.get();
        resetRepo.deleteByUserAndExpiresAtBefore(user, LocalDateTime.now());

        String code = genCode6();
        var token = PasswordResetToken.builder()
                .user(user)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        resetRepo.save(token);

        // email body (both code and link)
        String link = publicUrl + "/reset?email=" + url(user.getEmail()) + "&code=" + code;
        String html = """
            <div style="font-family:Arial,sans-serif;font-size:14px">
              <p>Use this code to reset your Birzum password:</p>
              <p style="font-size:20px;font-weight:bold;letter-spacing:2px">%s</p>
              <p>Or click this link (valid 15 minutes): <a href="%s">%s</a></p>
              <p>If you didn’t request this, ignore this email.</p>
            </div>
          """.formatted(code, link, link);

        emailService.sendHtml(user.getEmail(), "Reset your Birzum password", html);
    }

    // tiny helper
    private static String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        var user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or code"));

        // Find latest active tokens
        var tokens = resetRepo.findByUserAndUsedFalseOrderByExpiresAtDesc(user);
        var now = LocalDateTime.now();

        var match = tokens.stream()
                .filter(t -> t.getExpiresAt().isAfter(now))
                .filter(t -> passwordEncoder.matches(req.code(), t.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid email or code"));

        // mark all tokens used
        tokens.forEach(t -> t.setUsed(true));
        resetRepo.saveAll(tokens);

        // set new password
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

}
