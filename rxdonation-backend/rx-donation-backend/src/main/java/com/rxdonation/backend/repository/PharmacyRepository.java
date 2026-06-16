package com.rxdonation.backend.repository;

import com.rxdonation.backend.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    
    Optional<Pharmacy> findByUserId(Long userId);
}
