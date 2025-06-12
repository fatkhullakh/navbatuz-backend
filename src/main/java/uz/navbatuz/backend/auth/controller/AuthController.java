package uz.navbatuz.backend.auth.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
      "password": "securePassword"
    }
     */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    /*
      "email": "ali@example.com",
      "password": "securePassword"
     */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

}
