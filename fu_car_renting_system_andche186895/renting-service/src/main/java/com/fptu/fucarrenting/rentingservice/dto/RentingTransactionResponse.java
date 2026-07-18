package com.fptu.fucarrenting.rentingservice.dto;

import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RentingTransactionResponse {

    /*
     * Thông tin chính của giao dịch thuê.
     */
    private Long rentingTransactionId;

    private LocalDateTime rentingDate;

    private BigDecimal totalPrice;

    /*
     * Customer thuộc Customer Service,
     * Renting Service chỉ trả customerId.
     */
    private Long customerId;

    private RentingStatus rentingStatus;

    /*
     * Một transaction có một hoặc nhiều detail.
     */
    private List<RentingDetailResponse> details;
}