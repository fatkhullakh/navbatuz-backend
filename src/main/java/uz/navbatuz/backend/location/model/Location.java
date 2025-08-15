package uz.navbatuz.backend.location.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "locations",
        indexes = {
                @Index(name = "idx_location_city", columnList = "city"),
                @Index(name = "idx_location_district", columnList = "district"),
                @Index(name = "idx_location_country_iso2", columnList = "countryIso2")
        }
)
public class Location {

    @Id
    @GeneratedValue
    private UUID id;

    /** Free-form street line (e.g., “Yakkasaroy ko'chasi 12, ap. 45”) */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String addressLine1;

    /** Optional second line (building, entrance, landmark) */
    @Column(length = 255)
    private String addressLine2;

    /** Structured components to filter/search */
    @Column(length = 120) private String district;  // tumani / mahalla cluster if you store it here
    @NotBlank @Column(nullable = false, length = 120) private String city;

    /** ISO-3166 alpha-2 (e.g., "UZ") — store normalized, not “Uzbekistan” */
    @NotBlank @Pattern(regexp = "^[A-Z]{2}$")
    @Column(nullable = false, length = 2)
    private String countryIso2;

    @Column(length = 20)
    private String postalCode;

    /** True geo column; SRID 4326 (WGS84). Requires PostGIS. */
    @Column(columnDefinition = "GEOGRAPHY(POINT, 4326)", nullable = false)
    private Point point;

    /** Optional external IDs from geocoders (good for de-dup) */
    @Column(length = 128) private String provider;         // e.g., "osm", "google"
    @Column(length = 128) private String providerPlaceId;  // e.g., OSM/Google place id

    /** Soft flags */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Auditing */
    @CreationTimestamp @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
