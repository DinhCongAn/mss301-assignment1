package com.fptu.fucarrenting.carservice.controller;

import com.fptu.fucarrenting.carservice.dto.CarResponse;
import com.fptu.fucarrenting.carservice.dto.CreateCarRequest;
import com.fptu.fucarrenting.carservice.dto.UpdateCarRequest;
import com.fptu.fucarrenting.carservice.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(
            @Valid @RequestBody CreateCarRequest request
    ) {
        return carService.createCar(request);
    }

    @GetMapping
    public List<CarResponse> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{carId}")
    public CarResponse getCarById(
            @PathVariable Long carId
    ) {
        return carService.getCarById(carId);
    }

    @PutMapping("/{carId}")
    public CarResponse updateCar(
            @PathVariable Long carId,
            @Valid @RequestBody UpdateCarRequest request
    ) {
        return carService.updateCar(carId, request);
    }

    @DeleteMapping("/{carId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(
            @PathVariable Long carId
    ) {
        carService.deleteCar(carId);
    }

    /*
     * Khách hàng xem các xe đang hoạt động
     * và được phép cho thuê.
     */
    @GetMapping("/available")
    public List<CarResponse> getAvailableCars() {
        return carService.getAvailableCars();
    }
}