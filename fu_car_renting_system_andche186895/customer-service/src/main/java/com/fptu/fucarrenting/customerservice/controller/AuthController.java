package com.fptu.fucarrenting.customerservice.controller;

import com.fptu.fucarrenting.customerservice.dto.LoginRequest;
import com.fptu.fucarrenting.customerservice.dto.LoginResponse;
import com.fptu.fucarrenting.customerservice.dto.RegisterRequest;
import com.fptu.fucarrenting.customerservice.dto.RegisterResponse;
import com.fptu.fucarrenting.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return customerService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return customerService.login(request);
    }
}