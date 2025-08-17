package uz.navbatuz.backend.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;


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
                            user.getCountry()
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

    public List<UUID> getFavouriteProviders(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Customer not found"));
        return customer.getFavouriteShops();
    }

    public UUID requireCustomerIdByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"))
                .getId();
    }
}
