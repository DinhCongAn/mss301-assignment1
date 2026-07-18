package com.fptu.fucarrenting.rentingservice.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerEligibilityResponse {

    /*
     * ID Customer được Customer Service xác nhận.
     */
    private Long customerId;

    /*
     * true:
     * Customer được phép thuê xe.
     *
     * false:
     * Customer đang INACTIVE hoặc BLOCKED.
     */
    private boolean eligible;

    /*
     * Dùng String để Renting Service không phụ thuộc
     * trực tiếp vào enum CustomerStatus của
     * Customer Service.
     */
    private String customerStatus;

    /*
     * Thông báo chi tiết từ Customer Service.
     */
    private String message;
}