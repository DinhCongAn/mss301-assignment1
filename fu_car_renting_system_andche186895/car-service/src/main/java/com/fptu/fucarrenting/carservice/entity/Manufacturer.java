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
@Table(name = "manufacturers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_id")
    private Long manufacturerId;

    @Column(
            name = "manufacturer_name",
            nullable = false,
            unique = true,
            length = 100
    )
    private String manufacturerName;

    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    @Column(
            name = "manufacturer_country",
            length = 100
    )
    private String manufacturerCountry;
}