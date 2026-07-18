package com.fptu.fucarrenting.rentingservice.exception;

import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Xử lý khi không thể kết nối đến service khác
     * hoặc request bị timeout.
     */
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ProblemDetail>
    handleRetryableException(
            RetryableException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Customer Service or Car Service "
                                + "is currently unavailable"
                );

        problemDetail.setTitle(
                "Downstream service unavailable"
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problemDetail);
    }

    /*
     * Xử lý các lỗi Feign khác chưa được
     * FeignErrorDecoder chuyển đổi.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetail>
    handleFeignException(
            FeignException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_GATEWAY,
                        "An error occurred while communicating "
                                + "with another microservice"
                );

        problemDetail.setTitle(
                "Downstream communication error"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(problemDetail);
    }
}