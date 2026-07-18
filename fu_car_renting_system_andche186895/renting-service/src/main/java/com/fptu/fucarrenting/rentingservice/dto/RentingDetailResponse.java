package com.fptu.fucarrenting.rentingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RentingDetailResponse {

    /*
     * ID xe thuộc Car Service.
     */
    private Long carId;

    /*
     * Khoảng thời gian thuê xe.
     */
    private LocalDate startDate;

    private LocalDate endDate;

    /*
     * Giá thuê của riêng xe này
     * trong toàn bộ khoảng thời gian.
     */
    private BigDecimal price;
}