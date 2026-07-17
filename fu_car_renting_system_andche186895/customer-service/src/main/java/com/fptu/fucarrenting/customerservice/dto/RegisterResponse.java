package com.fptu.fucarrenting.customerservice.dto;

import com.fptu.fucarrenting.customerservice.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private Long customerId;

    private String customerName;

    private String telephone;

    private String email;

    private LocalDate customerBirthday;

    private CustomerStatus customerStatus;
}