package com.fptu.fucarrenting.rentingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RentingDetailId implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * Một phần của khóa chính kép.
     */
    @Column(name = "renting_transaction_id")
    private Long rentingTransactionId;

    /*
     * Car thuộc Car Service nên chỉ lưu carId.
     */
    @Column(name = "car_id")
    private Long carId;
}