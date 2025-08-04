package uz.navbatuz.backend.location.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue
    private UUID id;

    private String address;
    private String district;
    private String city;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
}
