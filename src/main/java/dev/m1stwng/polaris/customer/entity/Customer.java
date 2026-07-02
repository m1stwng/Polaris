package dev.m1stwng.polaris.customer.entity;

import dev.m1stwng.polaris.common.persistence.AuditableEntity;
import dev.m1stwng.polaris.identity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends AuditableEntity {

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 150, nullable = false)
    private String surname;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    LocalDate dateOfBirth;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
