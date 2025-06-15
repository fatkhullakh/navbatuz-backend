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
import uz.navbatuz.backend.user.model.Role;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.auth.service.JwtService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByPhoneNumber(req.getPhoneNumber()))
            throw new RuntimeException("Phone already in use");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already in use");

        User user = User.builder()
                .name(req.getName())
                .surname(req.getSurname())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .passwordHash(encoder.encode(req.getPassword()))
                .isActive(true)
                .role(Role.valueOf(req.getRole().toUpperCase()))
                .build();

        userRepo.save(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        return new AuthResponse(jwt.generateToken(claims, user, "24h"));

    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new BadCredentialsException("Invalid credentials");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        return new AuthResponse(jwt.generateToken(claims, user, "24h"));
    }
}


