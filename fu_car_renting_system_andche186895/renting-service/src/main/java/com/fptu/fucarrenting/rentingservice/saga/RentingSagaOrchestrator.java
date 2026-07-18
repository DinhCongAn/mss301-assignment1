package com.fptu.fucarrenting.rentingservice.saga;

import com.fptu.fucarrenting.rentingservice.dto.CreateRentingRequest;
import com.fptu.fucarrenting.rentingservice.dto.RentingTransactionResponse;
import com.fptu.fucarrenting.rentingservice.service.RentingPersistenceService;
import com.fptu.fucarrenting.rentingservice.service.RentingService;
import com.fptu.fucarrenting.rentingservice.service.model.CalculatedRentingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/*
 * Renting Service đóng vai trò trung tâm điều phối Saga.
 *
 * Orchestrator gọi các bước theo thứ tự,
 * xử lý kết quả và thực hiện compensation khi lỗi.
 */
@Component
@RequiredArgsConstructor
public class RentingSagaOrchestrator {

    private final RentingService rentingService;

    private final RentingPersistenceService
            rentingPersistenceService;

    public RentingTransactionResponse createRenting(
            Long customerId,
            CreateRentingRequest request
    ) {
        Long rentingTransactionId = null;

        try {
            /*
             * Bước 1:
             * Local transaction tạo trạng thái PENDING.
             */
            rentingTransactionId =
                    rentingPersistenceService
                            .createPendingTransaction(
                                    customerId
                            );

            /*
             * Bước 2:
             * Saga bắt đầu thực thi.
             */
            rentingPersistenceService
                    .markExecuting(
                            rentingTransactionId
                    );

            /*
             * Bước 3:
             * Gọi Customer Service.
             */
            rentingService
                    .validateCustomerEligibility(
                            customerId
                    );

            /*
             * Bước 4:
             * Gọi Car Service, kiểm tra ngày,
             * lịch thuê và tính giá.
             */
            List<CalculatedRentingItem>
                    calculatedItems =
                    rentingService
                            .validateAndCalculateItems(
                                    request
                            );

            /*
             * Bước 5:
             * Local transaction lưu detail,
             * totalPrice và chuyển COMPLETED.
             */
            return rentingPersistenceService
                    .completeTransaction(
                            rentingTransactionId,
                            calculatedItems
                    );

        } catch (RuntimeException originalException) {

            /*
             * Chỉ compensation khi PENDING đã được tạo.
             */
            if (rentingTransactionId != null) {
                try {
                    compensate(rentingTransactionId);

                } catch (RuntimeException
                        compensationException) {

                    originalException.addSuppressed(
                            compensationException
                    );

                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Renting failed and compensation "
                                    + "could not be completed",
                            originalException
                    );
                }
            }

            /*
             * Compensation thành công thì vẫn trả lại
             * lỗi nghiệp vụ ban đầu cho client.
             */
            throw originalException;
        }
    }

    private void compensate(
            Long rentingTransactionId
    ) {
        /*
         * EXECUTING/PENDING → FAILED
         */
        rentingPersistenceService
                .markFailed(rentingTransactionId);

        /*
         * FAILED → COMPENSATING
         */
        rentingPersistenceService
                .markCompensating(
                        rentingTransactionId
                );

        /*
         * Xóa dữ liệu chưa hoàn tất và chuyển:
         * COMPENSATING → COMPENSATED
         */
        rentingPersistenceService
                .completeCompensation(
                        rentingTransactionId
                );
    }
}