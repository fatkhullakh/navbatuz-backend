package uz.navbatuz.backend.provider.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.location.dto.LocationRequest;
import uz.navbatuz.backend.location.dto.LocationResponse;
import uz.navbatuz.backend.location.model.Location;
import uz.navbatuz.backend.provider.repository.BusinessHourRepository;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.dto.*;
import uz.navbatuz.backend.provider.model.BusinessHour;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final BusinessHourRepository businessHourRepository;
    private final CurrentUserService currentUserService;

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

    private BusinessHourResponse toResponse(BusinessHour bh) {
        return new BusinessHourResponse(
                bh.getId(),
                bh.getDay(),
                bh.getStartTime(),
                bh.getEndTime()
        );
    }

    public List<BusinessHourResponse> getBusinessHours(UUID providerId) {
        return businessHourRepository.findByProviderId(providerId).stream()
                .map(hour -> new BusinessHourResponse(
                        hour.getId(),
                        hour.getDay(),
                        hour.getStartTime(),
                        hour.getEndTime()
                ))
                .toList();
    }

    @Transactional
    public void updateBusinessHours(UUID providerId, List<BusinessHourRequest> requests) {
        if (requests.size() > 7) {
            throw new IllegalArgumentException("Cannot have more than 7 business hours.");
        }

        Set<DayOfWeek> uniqueDays = new HashSet<>();
        for (BusinessHourRequest req : requests) {
            if (!req.isValid()) {
                throw new IllegalArgumentException("Invalid time range for day: " + req.day());
            }

            if (!uniqueDays.add(req.day())) {
                throw new IllegalArgumentException("Duplicate day: " + req.day());
            }
        }

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        businessHourRepository.deleteByProviderId(providerId);

        List<BusinessHour> newHours = requests.stream()
                .map(req -> BusinessHour.builder()
                        .provider(provider)
                        .day(req.day())
                        .startTime(req.startTime())
                        .endTime(req.endTime())
                        .build()
                )
                .toList();

        businessHourRepository.saveAll(newHours);
    }

    public List<BusinessHourResponse> listForProvider(UUID providerId) {
        return businessHourRepository.findByProviderId(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void setBusinessHours(UUID providerId, List<BusinessHourRequest> requests) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        businessHourRepository.findByProviderId(providerId)
                .forEach(businessHourRepository::delete);

        List<BusinessHour> hours = requests.stream()
                .map(r -> BusinessHour.builder()
                        .provider(provider)
                        .day(r.day())
                        .startTime(r.startTime())
                        .endTime(r.endTime())
                        .build())
                .toList();

        businessHourRepository.saveAll(hours);
    }

    @Transactional
    public void updateLocation(UUID providerId, LocationRequest request) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        Location location = provider.getLocation();
        if (location == null) {
            location = new Location();
        }

        location.setAddress(request.address());
        location.setDistrict(request.district());
        location.setCity(request.city());
        location.setCountry(request.country());
        location.setPostalCode(request.postalCode());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());

        provider.setLocation(location);
        providerRepository.save(provider);
    }

    public LocationResponse getLocation(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        return new LocationResponse(
                provider.getLocation().getId(),
                provider.getLocation().getAddress(),
                provider.getLocation().getDistrict(),
                provider.getLocation().getCity(),
                provider.getLocation().getCountry(),
                provider.getLocation().getPostalCode(),
                provider.getLocation().getLatitude(),
                provider.getLocation().getLongitude()
        );
    }


}
