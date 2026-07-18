package com.fptu.fucarrenting.carservice.entity;

public enum CarStatus {

    /*
     * Xe đang hoạt động và được phép cho thuê.
     */
    AVAILABLE,

    /*
     * Xe đang được bảo trì hoặc sửa chữa.
     */
    MAINTENANCE,

    /*
     * Xe đã bị ngừng hoạt động.
     */
    INACTIVE
}