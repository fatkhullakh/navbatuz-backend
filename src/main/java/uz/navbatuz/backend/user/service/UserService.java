package uz.navbatuz.backend.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.user.dto.ChangePasswordRequest;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UserDetailsDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDetailsDTO(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getGender(),
                        user.getPhoneNumber(),
                        user.getEmail(),
                        user.getLanguage()
                ))
                .toList();
    }

    public UserDetailsDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDetailsDTO(
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getLanguage()
        );
    }

    public void updateUserById(UUID id, UserDetailsDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.findByEmail(request.getEmail()).ifPresent((User existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this email already exists.");
            }
        });

        userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent((User existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this phone number already exists.");
            }
        });

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setLanguage(request.getLanguage());
        userRepository.save(user);
    }

    public void deactivateUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }



}
