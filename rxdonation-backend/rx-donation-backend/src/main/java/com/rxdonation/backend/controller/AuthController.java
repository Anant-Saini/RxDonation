package com.rxdonation.backend.controller;


import com.rxdonation.backend.dto.requestDto.DonorRegistrationDto;
import com.rxdonation.backend.dto.requestDto.PharmacyRegistrationDto;
import com.rxdonation.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/donor")
    public ResponseEntity<String> registerDonor(@Valid @RequestBody DonorRegistrationDto registrationDto) {
            authService.registerDonor(registrationDto);
            return new ResponseEntity<>("Donor account registered successfully! Check your inbox for a verification email.", HttpStatus.CREATED);
    }

    @PostMapping("/register/pharmacy")
    public ResponseEntity<String> registerPharmacy(@Valid @RequestBody PharmacyRegistrationDto registrationDto) {
            authService.registerPharmacy(registrationDto);
            return new ResponseEntity<>("Pharmacy business registered successfully! Check your inbox for a verification email.", HttpStatus.CREATED);
    }
}
