package com.fptu.fucarrenting.carservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierResponse {

    private Long supplierId;

    private String supplierName;

    private String supplierDescription;

    private String supplierAddress;
}