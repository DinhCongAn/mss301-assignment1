package com.fptu.fucarrenting.rentingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class RentingReportResponse {

    /*
     * Khoảng ngày được Admin yêu cầu.
     */
    private LocalDate startDate;

    private LocalDate endDate;

    /*
     * Số giao dịch COMPLETED trong khoảng ngày.
     */
    private long totalTransactions;

    /*
     * Tổng doanh thu từ các giao dịch COMPLETED.
     */
    private BigDecimal totalRevenue;

    /*
     * Danh sách giao dịch được sắp xếp
     * theo rentingDate giảm dần.
     */
    private List<RentingTransactionResponse> transactions;
}