package uz.navbatuz.backend.auth.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.auth.dto.*;
import uz.navbatuz.backend.auth.rate.ForgotLimiter;
import uz.navbatuz.backend.auth.service.AuthService;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;
    private final ForgotLimiter forgotLimiter;



    /*
    POST /api/auth/register
    {
      "name": "Ali",
      "surname": "Karimov",
      "email": "ali@example.com",
      "phoneNumber": "998901234567",
      "password": "securePassword"
    }
     */

//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
//        return ResponseEntity.ok(authService.register(registerRequest));
//    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest requestRequest) {
        return ResponseEntity.ok(authService.register(requestRequest));
    }

    /*
      "email": "ali@example.com",
      "password": "securePassword"
     */

//    {
//        "name": "Fayzullakh",
//            "surname": "Turakhonov",
//            "dateOfBirth": "2006-03-26",
//            "gender": "MALE",
//            "phoneNumber": "+998998562588",
//            "email": "888@gmail.com",
//            "password": "Fayzi7267(?)",
//            "language": "UZ"
//    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        String email = req.email().trim().toLowerCase();
        if (!forgotLimiter.allow(email)) {
            org.slf4j.LoggerFactory.getLogger(getClass()).info("Forgot limiter BLOCKED {}", email);
            return ResponseEntity.noContent().build();
        }
        org.slf4j.LoggerFactory.getLogger(getClass()).info("Forgot accepted {}", email);
        authService.forgotPassword(req);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/email-exists")
    public ResponseEntity<Map<String, Boolean>> emailExists(@RequestParam String email) {
        boolean exists = authService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/public/phone-exists")
    public ResponseEntity<Map<String, Boolean>> phoneExists(@RequestParam String phone) {
        boolean exists = authService.phoneNumberExists(phone);
        return ResponseEntity.ok(Map.of("exists", exists));
    }


}