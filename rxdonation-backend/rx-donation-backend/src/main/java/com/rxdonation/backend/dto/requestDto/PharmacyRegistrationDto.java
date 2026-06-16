package com.rxdonation.backend.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PharmacyRegistrationDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Pharmacy business name is required")
        String pharmacyName,

        @NotBlank(message = "Contact telephone number is required")
        String telephone,

        @NotBlank(message = "Store address is required")
        String address,

        @NotNull(message = "Latitude is required")
        Double latitude,

        @NotNull(message = "Longitude is required")
        Double longitude,

        String openingTime, // Optional string parsed to LocalTime safely
        String closingTime  // Optional string parsed to LocalTime safely
) {}
