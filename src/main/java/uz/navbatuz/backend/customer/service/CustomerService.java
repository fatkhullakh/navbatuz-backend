package uz.navbatuz.backend.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.location.dto.LocationSummary;
import uz.navbatuz.backend.provider.dto.ProviderResponse;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    public List<UserDetailsDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> {
                    User user = c.getUser();
                    return new UserDetailsDTO(
                            user.getId(),
                            user.getName(),
                            user.getSurname(),
                            user.getDateOfBirth(),
                            user.getGender(),
                            user.getPhoneNumber(),
                            user.getEmail(),
                            user.getLanguage(),
                            user.getCountry(),
                            user.getAvatarUrl()
                    );
                })
                .toList();
    }

    public void addFavouriteProvider(String email, UUID providerId) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Customer not found"));

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(()-> new RuntimeException("Provider not found"));

        if (!customer.getFavouriteShops().contains(providerId)) {
            customer.getFavouriteShops().add(providerId);
        }
        customerRepository.save(customer);
    }

    public void removeFavouriteProvider(String email, UUID providerId) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Customer not found"));
        customer.getFavouriteShops().remove(providerId);
        customerRepository.save(customer);
    }

    public List<ProviderResponse> getFavouriteProviders(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<UUID> favIds = Optional.ofNullable(customer.getFavouriteShops()).orElseGet(List::of);
        if (favIds.isEmpty()) return List.of();

        List<Provider> providers = providerRepository.findAllById(favIds);
        Map<UUID, Provider> byId = providers.stream()
                .collect(Collectors.toMap(Provider::getId, Function.identity()));

        return favIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
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


    private ProviderResponse toResponse(Provider p) {
        return new ProviderResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getAvgRating(),
                p.getCategory(),
                toSummary(p.getLocation()),
                p.getLogoUrl()
        );
    }

    public UUID requireCustomerIdByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"))
                .getId();
    }
}
