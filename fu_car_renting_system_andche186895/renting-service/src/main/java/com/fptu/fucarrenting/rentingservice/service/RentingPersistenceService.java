package com.fptu.fucarrenting.rentingservice.service;

import com.fptu.fucarrenting.rentingservice.dto.RentingDetailResponse;
import com.fptu.fucarrenting.rentingservice.dto.RentingTransactionResponse;
import com.fptu.fucarrenting.rentingservice.entity.RentingDetail;
import com.fptu.fucarrenting.rentingservice.entity.RentingDetailId;
import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import com.fptu.fucarrenting.rentingservice.entity.RentingTransaction;
import com.fptu.fucarrenting.rentingservice.repository.RentingDetailRepository;
import com.fptu.fucarrenting.rentingservice.repository.RentingTransactionRepository;
import com.fptu.fucarrenting.rentingservice.service.model.CalculatedRentingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentingPersistenceService {

    private final RentingTransactionRepository
            rentingTransactionRepository;

    private final RentingDetailRepository
            rentingDetailRepository;

    /*
     * Các trạng thái làm cho lịch xe bị chiếm.
     */
    private static final List<RentingStatus>
            BLOCKING_STATUSES = List.of(
            RentingStatus.PENDING,
            RentingStatus.EXECUTING,
            RentingStatus.COMPLETED,
            RentingStatus.COMPENSATING
    );

    /*
     * Bước đầu của Saga.
     *
     * Tạo một RentingTransaction PENDING.
     * Chưa lưu RentingDetail vì chưa kiểm tra xe.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createPendingTransaction(
            Long customerId
    ) {
        RentingTransaction transaction =
                new RentingTransaction();

        transaction.setCustomerId(customerId);
        transaction.setTotalPrice(BigDecimal.ZERO);
        transaction.setRentingStatus(
                RentingStatus.PENDING
        );

        RentingTransaction savedTransaction =
                rentingTransactionRepository
                        .save(transaction);

        return savedTransaction
                .getRentingTransactionId();
    }

    /*
     * Chuyển PENDING sang EXECUTING.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExecuting(
            Long rentingTransactionId
    ) {
        changeStatus(
                rentingTransactionId,
                Set.of(RentingStatus.PENDING),
                RentingStatus.EXECUTING
        );
    }

    /*
     * Hoàn tất Saga:
     *
     * - kiểm tra trùng lịch lần cuối;
     * - lưu RentingDetail;
     * - cập nhật TotalPrice;
     * - chuyển trạng thái thành COMPLETED.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.SERIALIZABLE
    )
    public RentingTransactionResponse completeTransaction(
            Long rentingTransactionId,
            List<CalculatedRentingItem> calculatedItems
    ) {
        RentingTransaction transaction =
                findTransaction(rentingTransactionId);

        if (transaction.getRentingStatus()
                != RentingStatus.EXECUTING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Renting transaction is not executing"
            );
        }

        /*
         * Kiểm tra lại trong local transaction
         * trước khi lưu để hạn chế đặt trùng xe.
         */
        recheckOverlappingRentals(calculatedItems);

        BigDecimal totalPrice =
                calculatedItems
                        .stream()
                        .map(CalculatedRentingItem::price)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<RentingDetail> details =
                calculatedItems
                        .stream()
                        .map(item ->
                                createDetailEntity(
                                        transaction,
                                        item
                                )
                        )
                        .toList();

        List<RentingDetail> savedDetails =
                rentingDetailRepository
                        .saveAll(details);

        transaction.setTotalPrice(totalPrice);
        transaction.setRentingStatus(
                RentingStatus.COMPLETED
        );

        RentingTransaction completedTransaction =
                rentingTransactionRepository
                        .save(transaction);

        return toResponse(
                completedTransaction,
                savedDetails
        );
    }

    /*
     * Đánh dấu Saga thất bại.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            Long rentingTransactionId
    ) {
        RentingTransaction transaction =
                findTransaction(rentingTransactionId);

        /*
         * Giúp thao tác có tính idempotent.
         */
        if (transaction.getRentingStatus()
                == RentingStatus.FAILED
                || transaction.getRentingStatus()
                == RentingStatus.COMPENSATING
                || transaction.getRentingStatus()
                == RentingStatus.COMPENSATED) {
            return;
        }

        if (transaction.getRentingStatus()
                == RentingStatus.COMPLETED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A completed transaction cannot be marked as failed"
            );
        }

        transaction.setRentingStatus(
                RentingStatus.FAILED
        );

        rentingTransactionRepository.save(transaction);
    }

    /*
     * Chuyển FAILED sang COMPENSATING.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensating(
            Long rentingTransactionId
    ) {
        changeStatus(
                rentingTransactionId,
                Set.of(
                        RentingStatus.FAILED,
                        RentingStatus.COMPENSATING
                ),
                RentingStatus.COMPENSATING
        );
    }

    /*
     * Giao dịch bù của Renting Service.
     *
     * Chỉ sử dụng đúng hai bảng trong ERD:
     * RentingTransaction và RentingDetail.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeCompensation(
            Long rentingTransactionId
    ) {
        RentingTransaction transaction =
                findTransaction(rentingTransactionId);

        /*
         * Compensation được gọi lại nhiều lần
         * vẫn phải an toàn.
         */
        if (transaction.getRentingStatus()
                == RentingStatus.COMPENSATED) {
            return;
        }

        if (transaction.getRentingStatus()
                != RentingStatus.COMPENSATING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Renting transaction is not compensating"
            );
        }

        /*
         * Xóa RentingDetail nếu bước trước đã lưu
         * một phần dữ liệu.
         */
        List<RentingDetail> details =
                rentingDetailRepository
                        .findAllByRentingTransaction_RentingTransactionId(
                                rentingTransactionId
                        );

        if (!details.isEmpty()) {
            rentingDetailRepository.deleteAll(details);
        }

        transaction.setTotalPrice(BigDecimal.ZERO);
        transaction.setRentingStatus(
                RentingStatus.COMPENSATED
        );

        rentingTransactionRepository.save(transaction);
    }

    /*
     * Thay đổi trạng thái có kiểm soát.
     */
    private void changeStatus(
            Long rentingTransactionId,
            Set<RentingStatus> allowedStatuses,
            RentingStatus newStatus
    ) {
        RentingTransaction transaction =
                findTransaction(rentingTransactionId);

        /*
         * Nếu trạng thái đã đúng thì không cập nhật lại.
         */
        if (transaction.getRentingStatus()
                == newStatus) {
            return;
        }

        if (!allowedStatuses.contains(
                transaction.getRentingStatus()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid renting status transition from "
                            + transaction.getRentingStatus()
                            + " to "
                            + newStatus
            );
        }

        transaction.setRentingStatus(newStatus);

        rentingTransactionRepository.save(transaction);
    }

    private RentingTransaction findTransaction(
            Long rentingTransactionId
    ) {
        return rentingTransactionRepository
                .findById(rentingTransactionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Renting transaction not found"
                        )
                );
    }

    private RentingDetail createDetailEntity(
            RentingTransaction transaction,
            CalculatedRentingItem item
    ) {
        RentingDetailId id =
                new RentingDetailId(
                        transaction
                                .getRentingTransactionId(),
                        item.carId()
                );

        RentingDetail detail = new RentingDetail();

        detail.setId(id);
        detail.setRentingTransaction(transaction);
        detail.setStartDate(item.startDate());
        detail.setEndDate(item.endDate());
        detail.setPrice(item.price());

        return detail;
    }

    private void recheckOverlappingRentals(
            List<CalculatedRentingItem> calculatedItems
    ) {
        for (CalculatedRentingItem item
                : calculatedItems) {

            long overlappingCount =
                    rentingDetailRepository
                            .countOverlappingRentals(
                                    item.carId(),
                                    item.startDate(),
                                    item.endDate(),
                                    BLOCKING_STATUSES
                            );

            if (overlappingCount > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Car " + item.carId()
                                + " is already rented during "
                                + "the requested period"
                );
            }
        }
    }

    private RentingTransactionResponse toResponse(
            RentingTransaction transaction,
            List<RentingDetail> details
    ) {
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
}