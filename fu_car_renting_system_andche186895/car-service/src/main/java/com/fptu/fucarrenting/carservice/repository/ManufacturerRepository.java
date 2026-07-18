package com.fptu.fucarrenting.carservice.repository;

import com.fptu.fucarrenting.carservice.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManufacturerRepository
        extends JpaRepository<Manufacturer, Long> {

    Optional<Manufacturer> findByManufacturerNameIgnoreCase(
            String manufacturerName
    );

    boolean existsByManufacturerNameIgnoreCase(
            String manufacturerName
    );
}