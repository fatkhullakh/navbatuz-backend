package uz.navbatuz.backend.auth.service;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.auth.dto.AuthResponse;
import uz.navbatuz.backend.auth.dto.LoginRequest;
import uz.navbatuz.backend.auth.dto.RegisterRequest;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.MessageService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.auth.service.JwtService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public AuthResponse register(RegisterRequest request) {
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
                .language(request.getLanguage())
                .build();
        userRepository.save(user);

        Locale locale = switch (user.getLanguage()) {
            case UZ -> new Locale("uz");
            case RU -> new Locale("ru");
            default -> Locale.ENGLISH;
        };

        // Fetch localized message
        String message = messageService.get("user.created.success", locale);
        System.out.println("Localized message: " + message);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
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

}

