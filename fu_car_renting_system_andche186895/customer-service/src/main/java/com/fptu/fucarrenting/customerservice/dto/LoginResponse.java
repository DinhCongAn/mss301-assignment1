package com.fptu.fucarrenting.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private String role;

    private Long customerId;

    private String email;
}