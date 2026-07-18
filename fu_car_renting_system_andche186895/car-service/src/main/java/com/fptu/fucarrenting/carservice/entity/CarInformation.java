package com.fptu.fucarrenting.carservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "car_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    @Column(
            name = "car_name",
            nullable = false,
            length = 150
    )
    private String carName;

    @Column(
            name = "car_description",
            length = 2000
    )
    private String carDescription;

    @Column(
            name = "number_of_doors",
            nullable = false
    )
    private Integer numberOfDoors;

    @Column(
            name = "seating_capacity",
            nullable = false
    )
    private Integer seatingCapacity;

    @Column(
            name = "fuel_type",
            nullable = false,
            length = 50
    )
    private String fuelType;

    @Column(
            name = "year",
            nullable = false
    )
    private Integer year;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "manufacturer_id",
            nullable = false
    )
    private Manufacturer manufacturer;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "supplier_id",
            nullable = false
    )
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "car_status",
            nullable = false,
            length = 30
    )
    private CarStatus carStatus;

    @Column(
            name = "car_renting_price_per_day",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal carRentingPricePerDay;
}