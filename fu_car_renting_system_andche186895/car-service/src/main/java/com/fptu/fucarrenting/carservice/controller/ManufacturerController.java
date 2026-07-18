package com.fptu.fucarrenting.carservice.controller;

import com.fptu.fucarrenting.carservice.dto.ManufacturerRequest;
import com.fptu.fucarrenting.carservice.dto.ManufacturerResponse;
import com.fptu.fucarrenting.carservice.service.ManufacturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManufacturerResponse createManufacturer(
            @Valid @RequestBody ManufacturerRequest request
    ) {
        return manufacturerService.createManufacturer(request);
    }

    @GetMapping
    public List<ManufacturerResponse> getAllManufacturers() {
        return manufacturerService.getAllManufacturers();
    }
}