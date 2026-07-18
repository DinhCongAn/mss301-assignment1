package com.fptu.fucarrenting.carservice.repository;

import com.fptu.fucarrenting.carservice.entity.CarInformation;
import com.fptu.fucarrenting.carservice.entity.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarInformationRepository
        extends JpaRepository<CarInformation, Long> {

    List<CarInformation> findAllByCarStatus(
            CarStatus carStatus
    );

    List<CarInformation> findAllByCarStatusOrderByCarIdDesc(
            CarStatus carStatus
    );

    List<CarInformation> findAllByCarNameContainingIgnoreCase(
            String carName
    );

    List<CarInformation> findAllByOrderByCarIdDesc();
}