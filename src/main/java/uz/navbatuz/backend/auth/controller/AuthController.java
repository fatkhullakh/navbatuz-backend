package uz.navbatuz.backend.auth.controller;
import jakarta.validation.Valid;
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

}
