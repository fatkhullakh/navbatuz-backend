package uz.navbatuz.backend.user.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.user.dto.UserDetails;
import uz.navbatuz.backend.user.dto.UserResponseForWorker;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @RequestMapping
    public ResponseEntity<List<UserDetails>> getAllActiveUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @RequestMapping("/{id}")
    public ResponseEntity<UserDetails> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<UserDetails> updateUserById(@PathVariable UUID id, @Valid @RequestBody UserDetails userDetails) {
        userService.updateUserById(id, userDetails);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUserById(@PathVariable UUID id) {
        userService.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

}
