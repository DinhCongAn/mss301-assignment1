package com.fptu.fucarrenting.carservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(
            name = "supplier_name",
            nullable = false,
            unique = true,
            length = 150
    )
    private String supplierName;

    @Column(
            name = "supplier_description",
            length = 1000
    )
    private String supplierDescription;

    @Column(
            name = "supplier_address",
            length = 255
    )
    private String supplierAddress;
}