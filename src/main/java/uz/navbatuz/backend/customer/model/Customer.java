package uz.navbatuz.backend.customer.model;

import jakarta.persistence.*;
import lombok.*;
import uz.navbatuz.backend.user.model.User;

import java.util.List;
import java.util.UUID;



@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "customer_favourite_providers", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "provider_id")
    private List<UUID> favouriteShops;

}


//@Entity
//@DiscriminatorValue("CUSTOMER")
//@Getter
//@Setter
//@NoArgsConstructor
//public class Customer extends User {
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "customer_favourite_providers", joinColumns = @JoinColumn(name = "customer_id"))
//    @Column(name = "provider_id")
//    private List<UUID> favouriteShops;
//
//    // You can remove isActive, since it's already in User
//}
