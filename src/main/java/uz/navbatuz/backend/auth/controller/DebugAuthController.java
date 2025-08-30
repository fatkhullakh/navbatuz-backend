// uz/navbatuz/backend/auth/controller/DebugAuthController.java
package uz.navbatuz.backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class DebugAuthController {
    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(Authentication auth) {
        if (auth == null) return ResponseEntity.ok(Map.of("auth", "none"));
        return ResponseEntity.ok(Map.of(
                "principal", auth.getPrincipal(),
                "authorities", auth.getAuthorities().stream().map(Object::toString).collect(Collectors.toList())
        ));
    }
}
