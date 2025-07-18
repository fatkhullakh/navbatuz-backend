package uz.navbatuz.backend.customer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.customer.model.Customer;
import uz.navbatuz.backend.customer.service.CustomerService;
import uz.navbatuz.backend.user.dto.UserDetailsDTO;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/customers")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<UserDetailsDTO>> getAllCustomer() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping("/favourites/{providerId}")
    public ResponseEntity<Void> addFavouriteProvider(@PathVariable UUID providerId, Authentication authentication) {
        customerService.addFavouriteProvider(authentication.getName(), providerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favourites/{shopId}")
    public ResponseEntity<Void> removeFavouriteProvider(@PathVariable UUID shopId, Authentication authentication) {
        customerService.removeFavouriteProvider(authentication.getName(), shopId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favourites")
    public ResponseEntity<List<UUID>> getFavouriteProviders(Authentication authentication) {
        return ResponseEntity.ok(customerService.getFavouriteProviders(authentication.getName()));
    }
}
