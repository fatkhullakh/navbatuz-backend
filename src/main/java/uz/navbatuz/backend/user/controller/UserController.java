package uz.navbatuz.backend.user.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.user.dto.ChangePasswordRequest;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping
    public ResponseEntity<List<UserDetailsDTO>> getAllActiveUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<UserDetailsDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> updateUserById(@PathVariable UUID id, @Valid @RequestBody UserDetailsDTO userDetails) {
        userService.updateUserById(id, userDetails);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUserById(@PathVariable UUID id) {
        userService.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RECEPTIONIST', 'WORKER', 'ADMIN', 'CUSTOMER')")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        userService.changePassword(principal.getName(), request);
        return ResponseEntity.ok("Password updated successfully");
    }




}
