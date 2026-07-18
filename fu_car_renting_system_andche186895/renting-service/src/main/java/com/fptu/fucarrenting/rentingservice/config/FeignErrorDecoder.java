package com.fptu.fucarrenting.rentingservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    /*
     * Decoder mặc định của Feign.
     * Được dùng cho những trạng thái chưa xử lý riêng.
     */
    private final ErrorDecoder defaultErrorDecoder =
            new ErrorDecoder.Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response
    ) {
        String serviceName = resolveServiceName(methodKey);

        /*
         * Service đích trả request không hợp lệ.
         */
        if (response.status() == 400) {
            return new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    serviceName + " rejected the request"
            );
        }

        /*
         * Customer hoặc Car không tồn tại.
         */
        if (response.status() == 404) {
            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Requested resource was not found in "
                            + serviceName
            );
        }

        /*
         * Xung đột dữ liệu tại service đích.
         */
        if (response.status() == 409) {
            return new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Data conflict occurred in " + serviceName
            );
        }

        /*
         * Service đích có lỗi nội bộ.
         *
         * Renting Service trả 502 vì lỗi đến từ
         * một downstream service.
         */
        if (response.status() >= 500) {
            return new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    serviceName + " returned an internal error"
            );
        }

        return defaultErrorDecoder.decode(
                methodKey,
                response
        );
    }

    /*
     * methodKey thường có dạng:
     *
     * CustomerClient#checkEligibility(Long)
     * CarClient#getCarById(Long)
     */
    private String resolveServiceName(String methodKey) {

        if (methodKey.contains("CustomerClient")) {
            return "Customer Service";
        }

        if (methodKey.contains("CarClient")) {
            return "Car Service";
        }

        return "Downstream service";
    }
}