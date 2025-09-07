// src/main/java/uz/navbatuz/backend/user/model/DeletedUserArchive.java
package uz.navbatuz.backend.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import uz.navbatuz.backend.common.Gender;
import uz.navbatuz.backend.common.Language;
import uz.navbatuz.backend.common.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deleted_user_archive")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedUserArchive {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "original_user_id", nullable = false)
    private UUID originalUserId;

    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;

    private String name;
    private String surname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String country;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_user_id")
    private UUID deletedByUserId;

    @Column(name = "deleted_by_ip")
    private String deletedByIp;

    @Column(name = "reason")
    private String reason;
}