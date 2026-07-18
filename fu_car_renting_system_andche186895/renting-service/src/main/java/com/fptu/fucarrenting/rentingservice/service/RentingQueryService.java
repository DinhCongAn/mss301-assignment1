package com.fptu.fucarrenting.rentingservice.service;

import com.fptu.fucarrenting.rentingservice.dto.RentingDetailResponse;
import com.fptu.fucarrenting.rentingservice.dto.RentingReportResponse;
import com.fptu.fucarrenting.rentingservice.dto.RentingTransactionResponse;
import com.fptu.fucarrenting.rentingservice.entity.RentingDetail;
import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import com.fptu.fucarrenting.rentingservice.entity.RentingTransaction;
import com.fptu.fucarrenting.rentingservice.repository.RentingDetailRepository;
import com.fptu.fucarrenting.rentingservice.repository.RentingTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentingQueryService {

    private final RentingTransactionRepository
            rentingTransactionRepository;

    private final RentingDetailRepository
            rentingDetailRepository;

    /*
     * Customer xem lịch sử thuê của chính mình.
     *
     * Chỉ trả các giao dịch đã COMPLETED.
     */
    @Transactional(readOnly = true)
    public List<RentingTransactionResponse>
    getCustomerHistory(Long customerId) {

        validateCustomerId(customerId);

        return rentingTransactionRepository
                .findAllByCustomerIdAndRentingStatusOrderByRentingDateDesc(
                        customerId,
                        RentingStatus.COMPLETED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * Customer xem chi tiết một giao dịch.
     *
     * Customer chỉ được xem giao dịch thuộc
     * chính customerId của mình.
     */
    @Transactional(readOnly = true)
    public RentingTransactionResponse getCustomerTransaction(
            Long customerId,
            Long rentingTransactionId
    ) {
        validateCustomerId(customerId);

        if (rentingTransactionId == null
                || rentingTransactionId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Renting transaction ID is invalid"
            );
        }

        RentingTransaction transaction =
                rentingTransactionRepository
                        .findById(rentingTransactionId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Renting transaction not found"
                                )
                        );

        /*
         * Không cho Customer xem giao dịch
         * thuộc về Customer khác.
         */
        if (!customerId.equals(
                transaction.getCustomerId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view "
                            + "this renting transaction"
            );
        }

        /*
         * Lịch sử nghiệp vụ chỉ hiển thị
         * giao dịch thuê đã hoàn thành.
         */
        if (transaction.getRentingStatus()
                != RentingStatus.COMPLETED) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Completed renting transaction not found"
            );
        }

        return toResponse(transaction);
    }

    /*
     * Admin xem báo cáo giao dịch thành công
     * trong khoảng ngày được yêu cầu.
     */
    @Transactional(readOnly = true)
    public RentingReportResponse getReport(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateReportDates(startDate, endDate);

        /*
         * Bắt đầu từ 00:00 của StartDate.
         */
        LocalDateTime startInclusive =
                startDate.atStartOfDay();

        /*
         * Nhỏ hơn 00:00 của ngày kế tiếp EndDate.
         *
         * Ví dụ EndDate = 31/07 thì lấy toàn bộ
         * giao dịch trong ngày 31/07.
         */
        LocalDateTime endExclusive =
                endDate
                        .plusDays(1)
                        .atStartOfDay();

        List<RentingTransaction> transactions =
                rentingTransactionRepository
                        .findAllByRentingStatusAndRentingDateGreaterThanEqualAndRentingDateLessThanOrderByRentingDateDesc(
                                RentingStatus.COMPLETED,
                                startInclusive,
                                endExclusive
                        );

        BigDecimal totalRevenue =
                transactions
                        .stream()
                        .map(RentingTransaction::getTotalPrice)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<RentingTransactionResponse> responses =
                transactions
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new RentingReportResponse(
                startDate,
                endDate,
                transactions.size(),
                totalRevenue,
                responses
        );
    }

    /*
     * Chuyển RentingTransaction entity thành DTO.
     */
    private RentingTransactionResponse toResponse(
            RentingTransaction transaction
    ) {
        List<RentingDetail> details =
                rentingDetailRepository
                        .findAllByRentingTransaction_RentingTransactionId(
                                transaction
                                        .getRentingTransactionId()
                        );

        List<RentingDetailResponse> detailResponses =
                details
                        .stream()
                        .map(detail ->
                                new RentingDetailResponse(
                                        detail.getId().getCarId(),
                                        detail.getStartDate(),
                                        detail.getEndDate(),
                                        detail.getPrice()
                                )
                        )
                        .toList();

        return new RentingTransactionResponse(
                transaction.getRentingTransactionId(),
                transaction.getRentingDate(),
                transaction.getTotalPrice(),
                transaction.getCustomerId(),
                transaction.getRentingStatus(),
                detailResponses
        );
    }

    private void validateCustomerId(Long customerId) {

        if (customerId == null || customerId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer ID is invalid"
            );
        }
    }

    private void validateReportDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date must not be before start date"
            );
        }
    }
}