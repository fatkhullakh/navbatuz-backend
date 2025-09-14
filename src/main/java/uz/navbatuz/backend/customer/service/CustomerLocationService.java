package uz.navbatuz.backend.customer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.navbatuz.backend.customer.dto.CustomerLocationRequest;
import uz.navbatuz.backend.customer.dto.CustomerLocationResponse;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.repository.CustomerRepository;
import uz.navbatuz.backend.user.model.User;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.util.UUID;

import static uz.navbatuz.backend.common.geo.Geo.point;

@Service
@RequiredArgsConstructor
public class CustomerLocationService {

    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;

    private Customer requireByUserEmail(String email) {
        return customerRepo.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    public Customer requireById(UUID id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @Transactional
    public CustomerLocationResponse upsertForMe(String email, CustomerLocationRequest req) {
        var customer = customerRepo.findByUserEmail(email)
                .orElseGet(() -> {
                    // create Customer row if it doesn't exist yet
                    User u = userRepo.findByEmail(email)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                    return customerRepo.save(Customer.builder().id(u.getId()).user(u).build());
                });

        customer.setDefaultCenter(point(req.lat(), req.lon()));
        if (req.countryIso2() != null) customer.setCountryIso2(req.countryIso2().toUpperCase());
        if (req.city() != null)        customer.setCity(req.city());
        if (req.district() != null)    customer.setDistrict(req.district());

        var saved = customerRepo.save(customer);
        return toResponse(saved);
    }

    public CustomerLocationResponse getForMe(String email) {
        var c = requireByUserEmail(email);
        return toResponse(c);
    }

    public CustomerLocationResponse getByCustomerId(UUID id) {
        return toResponse(requireById(id));
    }

    private CustomerLocationResponse toResponse(Customer c) {
        Double lat = null, lon = null;
        if (c.getDefaultCenter() != null) {
            lat = c.getDefaultCenter().getY();
            lon = c.getDefaultCenter().getX();
        }
        return new CustomerLocationResponse(lat, lon, c.getCountryIso2(), c.getCity(), c.getDistrict());
    }
}
