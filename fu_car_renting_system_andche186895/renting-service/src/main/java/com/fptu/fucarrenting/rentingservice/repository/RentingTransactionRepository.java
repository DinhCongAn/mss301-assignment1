package com.fptu.fucarrenting.rentingservice.repository;

import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import com.fptu.fucarrenting.rentingservice.entity.RentingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RentingTransactionRepository
        extends JpaRepository<RentingTransaction, Long> {

    /*
     * Customer xem lịch sử thuê thành công của mình.
     *
     * Giao dịch mới nhất được hiển thị trước.
     */
    List<RentingTransaction>
    findAllByCustomerIdAndRentingStatusOrderByRentingDateDesc(
            Long customerId,
            RentingStatus rentingStatus
    );

    /*
     * Admin lấy các giao dịch thành công trong
     * một khoảng thời gian.
     *
     * Dùng khoảng:
     * startInclusive <= rentingDate < endExclusive
     *
     * Cách này bao gồm toàn bộ ngày EndDate
     * mà không phải xử lý 23:59:59.
     */
    List<RentingTransaction>
    findAllByRentingStatusAndRentingDateGreaterThanEqualAndRentingDateLessThanOrderByRentingDateDesc(
            RentingStatus rentingStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}