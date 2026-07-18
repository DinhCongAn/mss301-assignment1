package com.fptu.fucarrenting.carservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ManufacturerResponse {

    private Long manufacturerId;

    private String manufacturerName;

    private String description;

    private String manufacturerCountry;
}