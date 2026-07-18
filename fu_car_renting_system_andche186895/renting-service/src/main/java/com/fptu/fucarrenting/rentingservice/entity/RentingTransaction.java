package com.fptu.fucarrenting.rentingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "renting_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentingTransaction {

    /*
     * Khóa chính của giao dịch thuê.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "renting_transaction_id")
    private Long rentingTransactionId;

    /*
     * Thời điểm giao dịch thuê được tạo.
     */
    @Column(
            name = "renting_date",
            nullable = false
    )
    private LocalDateTime rentingDate;

    /*
     * Tổng tiền của toàn bộ các RentingDetail.
     */
    @Column(
            name = "total_price",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalPrice;

    /*
     * Customer thuộc Customer Service.
     *
     * Chỉ lưu ID tham chiếu, không tạo foreign key
     * hoặc quan hệ JPA xuyên microservice.
     */
    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;

    /*
     * Trạng thái của giao dịch và Saga.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "renting_status",
            nullable = false,
            length = 30
    )
    private RentingStatus rentingStatus;

    /*
     * Tự gán dữ liệu mặc định trước khi INSERT.
     */
    @PrePersist
    public void prePersist() {

        if (rentingDate == null) {
            rentingDate = LocalDateTime.now();
        }

        if (totalPrice == null) {
            totalPrice = BigDecimal.ZERO;
        }

        if (rentingStatus == null) {
            rentingStatus = RentingStatus.PENDING;
        }
    }
}