package com.fptu.fucarrenting.rentingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "renting_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentingDetail {

    /*
     * Khóa chính kép:
     * rentingTransactionId + carId.
     */
    @EmbeddedId
    private RentingDetailId id;

    /*
     * RentingTransaction và RentingDetail đều nằm
     * trong rental_db nên được dùng quan hệ JPA.
     *
     * @MapsId dùng giá trị rentingTransactionId
     * trong RentingDetailId.
     */
    @MapsId("rentingTransactionId")
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "renting_transaction_id",
            nullable = false
    )
    private RentingTransaction rentingTransaction;

    /*
     * Ngày bắt đầu thuê xe.
     */
    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;

    /*
     * Ngày kết thúc thuê xe.
     */
    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;

    /*
     * Tổng tiền của riêng xe này trong khoảng thuê.
     *
     * Sau này được tính:
     * giá mỗi ngày × số ngày thuê.
     */
    @Column(
            name = "price",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal price;
}