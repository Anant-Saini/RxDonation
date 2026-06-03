package com.rxdonation.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // This field is for the Pharmacy Unique ID you mentioned
    private String pharmacyUniqueId;

    private boolean isEmailVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
