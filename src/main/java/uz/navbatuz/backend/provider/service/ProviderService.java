package uz.navbatuz.backend.provider.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.common.Role;
import uz.navbatuz.backend.common.Status;
import uz.navbatuz.backend.location.dto.LocationRequest;
import uz.navbatuz.backend.location.dto.LocationResponse;
import uz.navbatuz.backend.location.dto.LocationSummary;
import uz.navbatuz.backend.location.model.Location;
import uz.navbatuz.backend.provider.repository.BusinessHourRepository;
import uz.navbatuz.backend.common.Category;
import uz.navbatuz.backend.provider.dto.*;
import uz.navbatuz.backend.provider.model.BusinessHour;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import uz.navbatuz.backend.receptionist.repository.ReceptionistRepository;
import uz.navbatuz.backend.security.CurrentUserService;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;
import uz.navbatuz.backend.worker.dto.WorkerDetailsDto;
import uz.navbatuz.backend.worker.dto.WorkerResponseForService;
import uz.navbatuz.backend.worker.mapper.WorkerMapper;
import uz.navbatuz.backend.worker.model.Worker;
import uz.navbatuz.backend.worker.repository.WorkerRepository;
import uz.navbatuz.backend.common.WorkerType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static uz.navbatuz.backend.common.Role.OWNER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final BusinessHourRepository businessHourRepository;
    private final CurrentUserService currentUserService;
    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final ReceptionistRepository receptionistRepository;

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

    private LocationSummary toSummary(uz.navbatuz.backend.location.model.Location loc) {
        if (loc == null) return null;
        return new LocationSummary(
                loc.getId(),
                loc.getAddressLine1(),
                loc.getCity(),
                loc.getCountryIso2()
        );
    }

    public ProviderResponse getById(UUID id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return new ProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getDescription(),
                provider.getAvgRating(),
                provider.getCategory(),
                toSummary(provider.getLocation()),
                provider.getLogoUrl()
        );
    }

    public ProvidersDetails getProvidersDetails(UUID id) {
        var provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        var workers = workerRepository.findWorkerResponsesByProviderId(id);
        var businessHours = businessHourRepository.findByProvider_Id(id);

        return new ProvidersDetails(
                provider.getId(),
                provider.getName(),
                provider.getDescription(),
                provider.getCategory(),
                workers,
                provider.getEmail(),
                provider.getPhoneNumber(),

                // NEW
                provider.getLogoUrl(),

                provider.getAvgRating(),
                businessHours,
                toSummary(provider.getLocation())
        );
    }

    public void updateById(UUID id, ProviderUpdateRequest request) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        providerRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this email already exists.");
            }
        });
        providerRepository.findByPhoneNumber(request.phoneNumber()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A provider with this phone number already exists.");
            }
        });

        provider.setName(request.name());
        provider.setDescription(request.description());
        provider.setCategory(request.category());
        provider.setEmail(request.email());
        provider.setPhoneNumber(request.phoneNumber());

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
                .map(p -> new ProviderResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getAvgRating(),
                        p.getCategory(),
                        toSummary(p.getLocation()), // <-- null-safe
                        p.getLogoUrl()
                ));
    }

    public List<ProviderResponse> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(p -> new ProviderResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getAvgRating(),
                        p.getCategory(),
                        toSummary(p.getLocation()), // <-- null-safe
                        p.getLogoUrl()
                ))
                .toList();
    }



//    public Provider findById(UUID id) {
//        return providerRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Provider not  found"));
//    }

//    public void deactivate(UUID id) {
//        log.info("Start deactivating provider with id: {}", id);
//        Provider provider = providerRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));
//        provider.setActive(false);
//        providerRepository.save(provider);
//        log.info("Successfully deactivated provider with id: {}", id);
//    }

    public void deactivate(UUID providerId, UUID currentUserId) {
        log.info("Start deactivating provider with id: {} by user: {}", providerId, currentUserId);

        // Find the provider
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        // Find the current user
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Authorization checks
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().name());
        boolean isOwner = provider.getOwner() != null && provider.getOwner().getId().equals(currentUserId);
        boolean isReceptionist = false;

        // Check if user is a receptionist of this provider
        if (currentUser.getRole() != null && "RECEPTIONIST".equals(currentUser.getRole().name())) {
            isReceptionist = receptionistRepository.findByUserId(currentUserId)
                    .map(receptionist -> receptionist.getProvider().getId().equals(providerId))
                    .orElse(false);
        }

        if (!isAdmin && !isOwner && !isReceptionist) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to deactivate this provider");
        }

        // Perform the deactivation
        provider.setActive(false);
        providerRepository.save(provider);

        log.info("Successfully deactivated provider with id: {} by user: {}", providerId, currentUserId);
    }


    public Page<ProviderResponse> searchByCategory(Category category, Pageable pageable) {
        return providerRepository.findByCategoryAndIsActiveTrue(category, pageable)
                .map(p -> new ProviderResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getAvgRating(),
                        p.getCategory(),
                        toSummary(p.getLocation()), // <-- null-safe
                        p.getLogoUrl()
                ));
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

    private static final GeometryFactory GEO_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public static Point makePoint(Double lat, Double lng) {
        if (lat == null || lng == null) throw new IllegalArgumentException("lat/lng required");
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("latitude out of range");
        if (lng < -180 || lng > 180) throw new IllegalArgumentException("longitude out of range");
        Point p = GEO_FACTORY.createPoint(new Coordinate(lng, lat)); // x=lng, y=lat
        p.setSRID(4326);
        return p;
    }

    public static String normIso2(String iso2) {
        return iso2 == null ? null : iso2.trim().toUpperCase();
    }

    @Transactional
    public void updateLocation(UUID providerId, LocationRequest req) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        Location loc = provider.getLocation();
        if (loc == null) {
            loc = new Location();
        }

        // set structured + free-form
        loc.setAddressLine1(req.addressLine1());
        loc.setAddressLine2(req.addressLine2());
        loc.setDistrict(req.district());
        loc.setCity(req.city());
        loc.setCountryIso2(normIso2(req.countryIso2()));
        loc.setPostalCode(req.postalCode());

        // set geo point (WGS84)
        loc.setPoint(makePoint(req.latitude(), req.longitude()));

        // optional external geocoder IDs
        loc.setProvider(req.provider());
        loc.setProviderPlaceId(req.providerPlaceId());

        // activate by default on update
        if (loc.getId() == null) {
            loc.setActive(true);
        }

        // attach to provider (make sure Provider owns the relation)
        provider.setLocation(loc);
        providerRepository.save(provider);
    }


    public LocationResponse getLocation(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        Location loc = provider.getLocation();
        if (loc == null) {
            throw new EntityNotFoundException("Location not set for provider");
        }

        Double lat = null, lng = null;
        if (loc.getPoint() != null) {
            lat = loc.getPoint().getY(); // y = lat
            lng = loc.getPoint().getX(); // x = lng
        }

        return new LocationResponse(
                loc.getId(),
                loc.getAddressLine1(),
                loc.getAddressLine2(),
                loc.getDistrict(),
                loc.getCity(),
                loc.getCountryIso2(),
                loc.getPostalCode(),
                lat,
                lng,
                loc.isActive(),
                loc.getCreatedAt(),
                loc.getUpdatedAt()
        );
    }

    public LocationSummary getLocationSummary(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found"));

        return new LocationSummary(
                provider.getLocation().getId(),
                provider.getLocation().getAddressLine1(),
                provider.getLocation().getCity(),
                provider.getLocation().getCountryIso2()
        );
    }


    @Transactional
    public Provider updateLogo(UUID providerId, String url, UUID actorId) {
        Provider p = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
        var actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor not found"));

        boolean isOwner = p.getOwner() != null && p.getOwner().getId().equals(actorId);
        boolean isAdmin = actor.getRole() != null && actor.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this provider");
        }

        p.setLogoUrl(url);
        return providerRepository.save(p);
    }

    @Transactional
    public void setLogoUrl(UUID providerId, String url /*, String requesterEmail */) {
        var p = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        // Optional: verify requester is the owner of this provider
        p.setLogoUrl(url);
        providerRepository.save(p);
    }

    public UUID getProviderIdForOwner(UUID userId) {
        return providerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for owner"))
                .getId();
    }

    public UUID getProviderIdForReceptionist(UUID userId) {
        return receptionistRepository.findByUserId(userId)
                .map(r -> {
                    // depending on your model:
                    // return r.getProvider().getId();
                    return r.getProvider().getId(); // if you store providerId directly
                })
                .orElseThrow(() -> new RuntimeException("Provider not found for receptionist"));
    }


//    public UUID getProviderIdForReceptionist(UUID userId) {
//        return receptionistRepository.findByReceptionistId(userId)
//                .orElseThrow(() -> new RuntimeException("Provider not found for receptionist"))
//                .getProvider().getId();
//    }
//
//    public UUID getProviderIdForWorker(UUID userId) {
//        return workerRepository.findByWorkerId(userId)
//                .orElseThrow(() -> new RuntimeException("Provider not found for worker"))
//                .getProvider().getId();
//    }

    public UUID getProviderIdForUser(UUID userId, Role role) {
        return switch (role) {
            case OWNER -> getProviderIdForOwner(userId);
            case RECEPTIONIST -> getProviderIdForReceptionist(userId);
            //case WORKER -> getProviderIdForWorker(userId);
            default -> throw new IllegalStateException("Role not linked to provider: " + role);
        };
    }

    @Transactional
    public WorkerDetailsDto enable(UUID providerId, EnableOwnerAsWorkerRequest req) {
        UUID userId = currentUserService.getCurrentUserId();

        Provider p = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        // only the real owner can enable themselves
        if (p.getOwner() == null || !p.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your provider");
        }

        // if a worker already exists for this (user, provider), use that managed entity
        Worker worker = workerRepository.findByUser_IdAndProvider_Id(userId, providerId).orElse(null);

        if (worker == null) {
            // if a Worker with this id exists on a DIFFERENT provider, block
            Worker existingById = workerRepository.findById(userId).orElse(null);
            if (existingById != null && !existingById.getProvider().getId().equals(providerId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already work for another provider");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            worker = Worker.builder()
                    .id(userId)                      // @MapsId → same as user id
                    .user(user)
                    .provider(p)
                    .workerType(defaultType(req))
                    .status(defaultStatus(req))
                    .isActive(req.isActive() == null || req.isActive()) // default true
                    .hireDate(LocalDate.now())
                    .avgRating(0f)
                    .reviewsCount(0L)
                    .build();

            worker = workerRepository.save(worker); // managed persist
        } else {
            // update fields on the managed instance (NO merge)
            if (req.workerType() != null) worker.setWorkerType(req.workerType());
            if (req.status() != null) worker.setStatus(req.status());
            if (req.isActive() != null) worker.setActive(req.isActive());
            // hireDate stays as is if already set
            worker = workerRepository.save(worker);
        }

        return workerMapper.mapToDetails(worker);
    }

    private WorkerType defaultType(EnableOwnerAsWorkerRequest req) {
        return req.workerType() != null ? req.workerType() : WorkerType.GENERAL;
    }

    private Status defaultStatus(EnableOwnerAsWorkerRequest req) {
        return req.status() != null ? req.status() : Status.AVAILABLE;
    }

    public boolean emailExists(String email) {
        return providerRepository.existsByEmailIgnoreCase(email);
    }

    public boolean phoneNumberExists(String phoneNumber) {
        return providerRepository.existsByPhoneNumber(phoneNumber);
    }
}
