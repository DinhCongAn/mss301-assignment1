package com.fptu.fucarrenting.carservice.dto;

import com.fptu.fucarrenting.carservice.entity.CarStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCarRequest {

    /*
     * Tên xe bắt buộc phải có.
     */
    @NotBlank(message = "Car name is required")
    @Size(
            max = 150,
            message = "Car name must not exceed 150 characters"
    )
    private String carName;

    /*
     * Mô tả xe không bắt buộc.
     */
    @Size(
            max = 2000,
            message = "Car description must not exceed 2000 characters"
    )
    private String carDescription;

    /*
     * Số cửa của xe.
     */
    @NotNull(message = "Number of doors is required")
    @Min(
            value = 2,
            message = "Number of doors must be at least 2"
    )
    @Max(
            value = 6,
            message = "Number of doors must not exceed 6"
    )
    private Integer numberOfDoors;

    /*
     * Số chỗ ngồi phải lớn hơn 0.
     */
    @NotNull(message = "Seating capacity is required")
    @Positive(message = "Seating capacity must be greater than 0")
    private Integer seatingCapacity;

    /*
     * Ví dụ:
     * GASOLINE, DIESEL, ELECTRIC, HYBRID.
     */
    @NotBlank(message = "Fuel type is required")
    @Size(
            max = 50,
            message = "Fuel type must not exceed 50 characters"
    )
    private String fuelType;

    /*
     * Năm sản xuất.
     */
    @NotNull(message = "Manufacture year is required")
    @Min(
            value = 1886,
            message = "Manufacture year must be at least 1886"
    )
    @Max(
            value = 2100,
            message = "Manufacture year must not exceed 2100"
    )
    private Integer year;

    /*
     * Hãng sản xuất mới của xe phải tồn tại.
     */
    @NotNull(message = "Manufacturer ID is required")
    @Positive(message = "Manufacturer ID must be greater than 0")
    private Long manufacturerId;

    /*
     * Nhà cung cấp mới phải tồn tại.
     */
    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be greater than 0")
    private Long supplierId;

    /*
     * Trạng thái xe.
     */
    @NotNull(message = "Car status is required")
    private CarStatus carStatus;

    /*
     * Giá thuê theo ngày.
     */
    @NotNull(message = "Car renting price per day is required")
    @DecimalMin(
            value = "0.01",
            message = "Car renting price per day must be greater than 0"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Car renting price has an invalid format"
    )
    private BigDecimal carRentingPricePerDay;
}