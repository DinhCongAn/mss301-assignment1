package com.fptu.fucarrenting.carservice.dto;

import com.fptu.fucarrenting.carservice.entity.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CarResponse {

    private Long carId;

    private String carName;

    private String carDescription;

    private Integer numberOfDoors;

    private Integer seatingCapacity;

    private String fuelType;

    private Integer year;

    private Long manufacturerId;

    private String manufacturerName;

    private Long supplierId;

    private String supplierName;

    private CarStatus carStatus;

    private BigDecimal carRentingPricePerDay;
}