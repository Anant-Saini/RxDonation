package com.rxdonation.backend.service;

import com.rxdonation.backend.dto.requestDto.DonorRegistrationDto;
import com.rxdonation.backend.dto.requestDto.PharmacyRegistrationDto;
import com.rxdonation.backend.model.*;
import com.rxdonation.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // GeometryFactory initializing the WGS 84 (SRID 4326) coordinate space
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public void registerDonor(DonorRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email is already registered!");
        }

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(UserRole.DONOR) // Fixed mapping implicitly
                .isVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        Point point = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));

        Donor donor = Donor.builder()
                .user(savedUser)
                .name(dto.name())
                .address(dto.address())
                .location(point)
                .build();
        donorRepository.save(donor);
    }

    @Transactional
    public void registerPharmacy(PharmacyRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email is already registered!");
        }

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(UserRole.PHARMACY) // Fixed mapping implicitly
                .isVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        Point point = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));

        Pharmacy pharmacy = Pharmacy.builder()
                .user(savedUser)
                .pharmacyName(dto.pharmacyName())
                .telephone(dto.telephone())
                .address(dto.address())
                .location(point)
                .openingTime(dto.openingTime() != null ? LocalTime.parse(dto.openingTime()) : null)
                .closingTime(dto.closingTime() != null ? LocalTime.parse(dto.closingTime()) : null)
                .build();
        pharmacyRepository.save(pharmacy);
    }
}
