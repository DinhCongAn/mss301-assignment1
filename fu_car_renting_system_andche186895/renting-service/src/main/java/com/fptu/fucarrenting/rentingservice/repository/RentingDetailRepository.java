package com.fptu.fucarrenting.rentingservice.repository;

import com.fptu.fucarrenting.rentingservice.entity.RentingDetail;
import com.fptu.fucarrenting.rentingservice.entity.RentingDetailId;
import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RentingDetailRepository
        extends JpaRepository<RentingDetail, RentingDetailId> {

    /*
     * Lấy toàn bộ chi tiết của một giao dịch thuê.
     */
    List<RentingDetail>
    findAllByRentingTransaction_RentingTransactionId(
            Long rentingTransactionId
    );

    /*
     * Đếm số lịch thuê của một xe bị trùng
     * với khoảng ngày được yêu cầu.
     */
    @Query("""
        SELECT COUNT(detail)
        FROM RentingDetail detail
        WHERE detail.id.carId = :carId
          AND detail.rentingTransaction.rentingStatus
              IN :blockingStatuses
          AND detail.startDate <= :endDate
          AND detail.endDate >= :startDate
        """)
    long countOverlappingRentals(
            @Param("carId")
            Long carId,

            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate,

            @Param("blockingStatuses")
            Collection<RentingStatus> blockingStatuses
    );
}