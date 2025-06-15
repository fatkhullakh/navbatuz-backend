package uz.navbatuz.backend.auth.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.auth.dto.AuthResponse;
import uz.navbatuz.backend.auth.dto.LoginRequest;
import uz.navbatuz.backend.auth.dto.RegisterRequest;
import uz.navbatuz.backend.auth.service.AuthService;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;



    /*
    POST /api/auth/register
    {
      "name": "Ali",
      "surname": "Karimov",
      "email": "ali@example.com",
      "phoneNumber": "998901234567",
      "password": "securePassword",
      "role": "ADMIN"
    }

     */

//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
//        return ResponseEntity.ok(authService.register(registerRequest));
//    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /*
      "email": "ali@example.com",
      "password": "securePassword"
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/protected-test")
    @PreAuthorize("hasRole('ADMIN')") // Example
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("success");
    }

}