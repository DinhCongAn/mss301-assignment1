package com.fptu.fucarrenting.rentingservice.entity;

/*
 * Các trạng thái bám theo phần State Management
 * của Saga Pattern trong tài liệu môn học.
 */
public enum RentingStatus {

    /*
     * Giao dịch đã được khởi tạo,
     * nhưng quy trình chưa bắt đầu.
     */
    PENDING,

    /*
     * Saga đang kiểm tra Customer,
     * Car Service và lịch thuê.
     */
    EXECUTING,

    /*
     * Giao dịch thuê đã hoàn tất thành công.
     */
    COMPLETED,

    /*
     * Một bước trong Saga gặp lỗi.
     */
    FAILED,

    /*
     * Saga đang thực hiện giao dịch bù.
     */
    COMPENSATING,

    /*
     * Giao dịch bù đã hoàn tất.
     */
    COMPENSATED
}