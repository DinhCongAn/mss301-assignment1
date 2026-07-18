package com.fptu.fucarrenting.rentingservice.controller;

import com.fptu.fucarrenting.rentingservice.dto.CreateRentingRequest;
import com.fptu.fucarrenting.rentingservice.dto.RentingReportResponse;
import com.fptu.fucarrenting.rentingservice.dto.RentingTransactionResponse;
import com.fptu.fucarrenting.rentingservice.saga.RentingSagaOrchestrator;
import com.fptu.fucarrenting.rentingservice.service.RentingQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rentings")
@RequiredArgsConstructor
public class RentingController {

    private final RentingSagaOrchestrator
            rentingSagaOrchestrator;

    private final RentingQueryService
            rentingQueryService;

    /*
     * Customer tạo giao dịch thuê.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentingTransactionResponse createRenting(
            @RequestHeader("X-Customer-Id")
            Long customerId,

            @Valid
            @RequestBody
            CreateRentingRequest request
    ) {
        return rentingSagaOrchestrator
                .createRenting(
                        customerId,
                        request
                );
    }

    /*
     * Customer xem lịch sử thuê của chính mình.
     */
    @GetMapping("/history")
    public List<RentingTransactionResponse>
    getCustomerHistory(
            @RequestHeader("X-Customer-Id")
            Long customerId
    ) {
        return rentingQueryService
                .getCustomerHistory(customerId);
    }

    /*
     * Admin xem báo cáo theo khoảng ngày.
     *
     * API Gateway xác thực JWT và tự động truyền
     * vai trò người dùng qua header X-Role.
     */
    @GetMapping("/reports")
    public RentingReportResponse getReport(
            @RequestHeader("X-Role")
            String role,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate
    ) {
        /*
         * Kiểm tra tạm thời trong giai đoạn
         * chưa hoàn thiện API Gateway.
         */
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin role is required"
            );
        }

        return rentingQueryService.getReport(
                startDate,
                endDate
        );
    }

    /*
     * Customer xem chi tiết một giao dịch
     * thuộc về chính mình.
     *
     * Đặt endpoint cố định /history và /reports
     * trước endpoint có path variable để dễ đọc.
     */
    @GetMapping("/{rentingTransactionId}")
    public RentingTransactionResponse
    getCustomerTransaction(
            @RequestHeader("X-Customer-Id")
            Long customerId,

            @PathVariable
            Long rentingTransactionId
    ) {
        return rentingQueryService
                .getCustomerTransaction(
                        customerId,
                        rentingTransactionId
                );
    }
}