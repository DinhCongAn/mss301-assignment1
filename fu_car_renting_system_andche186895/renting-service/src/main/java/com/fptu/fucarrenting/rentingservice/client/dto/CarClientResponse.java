package com.fptu.fucarrenting.rentingservice.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CarClientResponse {

    /*
     * ID xe được Car Service xác nhận.
     */
    private Long carId;

    /*
     * Tên xe dùng để hiển thị kết quả
     * hoặc thông báo lỗi.
     */
    private String carName;

    /*
     * AVAILABLE, MAINTENANCE hoặc INACTIVE.
     *
     * Dùng String để không phụ thuộc vào
     * enum CarStatus của Car Service.
     */
    private String carStatus;

    /*
     * Giá thuê theo ngày do Car Service quản lý.
     *
     * Renting Service dùng giá này để tính tiền,
     * không nhận giá do client gửi lên.
     */
    private BigDecimal carRentingPricePerDay;
}