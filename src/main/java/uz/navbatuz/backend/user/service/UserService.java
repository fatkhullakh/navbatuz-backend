package uz.navbatuz.backend.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.provider.dto.ProviderResponse;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.user.dto.UserDetails;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDetails> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDetails(
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

    public UserDetails getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDetails(
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getLanguage()
        );
    }

    public void updateUserById(UUID id, UserDetails request) {
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


}
