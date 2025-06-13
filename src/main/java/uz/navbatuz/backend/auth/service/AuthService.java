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
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.auth.service.JwtService;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository; //  Interacts with your user database (save, find by email).
    private final PasswordEncoder passwordEncoder; // Encrypts and verifies passwords.
    private final JwtService jwtService; // Custom service that handles JWT token creation.

    /*
    Converts incoming request into a User object.
    Uses passwordEncoder.encode() to hash the password (never store raw passwords).
    Sets isActive = true.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use");
        }
        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();
        userRepository.save(user); //Saves the new user into the database.

        String token = jwtService.generateToken(user.getEmail()); //Generates a token based on user's email.
        return new AuthResponse(token); // Returns it wrapped in an AuthResponse.
    }


    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Wrong password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

}

