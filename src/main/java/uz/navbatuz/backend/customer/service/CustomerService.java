package uz.navbatuz.backend.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;
import uz.navbatuz.backend.user.model.User;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;

    public List<UserDetailsDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> {
                    User user = c.getUser();
                    return new UserDetailsDTO(
                            user.getName(),
                            user.getSurname(),
                            user.getDateOfBirth(),
                            user.getGender(),
                            user.getPhoneNumber(),
                            user.getEmail(),
                            user.getLanguage()
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
}
