package com.fptu.fucarrenting.rentingservice.client;

import com.fptu.fucarrenting.rentingservice.client.dto.CarClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * name phải trùng với:
 *
 * spring:
 *   application:
 *     name: car-service
 *
 * Eureka sẽ cung cấp địa chỉ thật của Car Service.
 */
@FeignClient(name = "car-service")
public interface CarClient {

    /*
     * Lấy thông tin xe từ Car Service.
     */
    @GetMapping("/cars/{carId}")
    CarClientResponse getCarById(
            @PathVariable("carId") Long carId
    );
}