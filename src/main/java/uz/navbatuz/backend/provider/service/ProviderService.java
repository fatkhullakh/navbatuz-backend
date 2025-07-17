package uz.navbatuz.backend.provider.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.dto.ProviderRequest;
import uz.navbatuz.backend.provider.dto.ProviderResponse;
import uz.navbatuz.backend.provider.dto.ProvidersDetails;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    public Provider create(ProviderRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + request.getOwnerId()));

        if (providerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A provider with this email already exists.");
        }
        if (providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("A provider with this phone number already exists.");
        }
        Provider provider = Provider.builder()
                .name(request.getName())
                .description(request.getDescription())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .category(request.getCategory())
                .teamSize(request.getTeamSize())
                .isActive(true)
                .avgRating(0f)
                .owner(owner)
                .build();

        return providerRepository.save(provider);
    }


    public ProvidersDetails getById(UUID id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return new ProvidersDetails(
                provider.getName(),
                provider.getDescription(),
                provider.getCategory(),
                provider.getTeamSize(),
                provider.getEmail(),
                provider.getPhoneNumber(),
                provider.getAvgRating()
        );
    }

    public void updateById(UUID id, ProviderRequest request) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        providerRepository.findByEmail(request.getEmail()).ifPresent((Provider existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this email already exists.");
            }
        });

        providerRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent((Provider existing) -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this phone number already exists.");
            }
        });


        provider.setName(request.getName());
        provider.setDescription(request.getDescription());
        provider.setCategory(request.getCategory());
        provider.setTeamSize(request.getTeamSize());
        provider.setEmail(request.getEmail());
        provider.setPhoneNumber(request.getPhoneNumber());

        providerRepository.save(provider);
    }

//    public List<ProviderResponse> getAllActiveProviders() {
//        return providerRepository.findAllByIsActiveTrue().stream()
//                .map(provider -> new ProviderResponse(
//                        provider.getName(),
//                        provider.getDescription(),
//                        provider.getAvgRating()
//                ))
//                .toList();
//    }
    public Page<ProviderResponse> getAllActiveProviders(Pageable pageable) {
        return providerRepository.findByIsActiveTrue(pageable)
                .map(provider -> new ProviderResponse(
                        provider.getName(),
                        provider.getDescription(),
                        provider.getAvgRating()
                ));
    }


    public List<ProviderResponse> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(provider -> new ProviderResponse(
                        provider.getName(),
                        provider.getDescription(),
                        provider.getAvgRating()
                ))
                .toList();
    }


//    public Provider findById(UUID id) {
//        return providerRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Provider not  found"));
//    }

    public void deactivate(UUID id) {
        log.info("Start deactivating provider with id: {}", id);
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        provider.setActive(false);
        providerRepository.save(provider);
        log.info("Successfully deactivated provider with id: {}", id);
    }




    public Page<ProviderResponse> searchByCategory(Category category, Pageable pageable) {
        return providerRepository.findByCategoryAndIsActiveTrue(category, pageable)
                .map(p -> new ProviderResponse(p.getName(), p.getDescription(), p.getAvgRating()));
    }


}
