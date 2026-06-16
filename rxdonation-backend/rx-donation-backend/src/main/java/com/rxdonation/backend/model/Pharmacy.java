package com.rxdonation.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalTime;

@Entity
@Table(name = "pharmacies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_pharmacy_user"))
    private User user;

    @Column(name = "pharmacy_name", nullable = false)
    private String pharmacyName;

    @Column(nullable = false)
    private String telephone;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(columnDefinition = "GEOMETRY(Point, 4326)", nullable = false)
    private Point location;
}
