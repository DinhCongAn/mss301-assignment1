package com.fptu.fucarrenting.customerservice.controller;

import com.fptu.fucarrenting.customerservice.dto.CustomerResponse;
import com.fptu.fucarrenting.customerservice.dto.RegisterRequest;
import com.fptu.fucarrenting.customerservice.dto.RegisterResponse;
import com.fptu.fucarrenting.customerservice.dto.UpdateCustomerStatusRequest;
import com.fptu.fucarrenting.customerservice.entity.CustomerStatus;
import com.fptu.fucarrenting.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;

    /*
     * Admin tạo tài khoản Customer mới.
     * Sử dụng lại nghiệp vụ đăng ký có sẵn.
     * Customer mới mặc định có trạng thái ACTIVE.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createCustomer(
            @Valid @RequestBody RegisterRequest request
    ) {
        return customerService.register(request);
    }

    /*
     * Admin xem danh sách toàn bộ Customer.
     */
    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    /*
     * Admin xem thông tin một Customer theo ID.
     */
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(
            @PathVariable Long customerId
    ) {
        return customerService.getCustomerById(customerId);
    }

    /*
     * Admin thay đổi trạng thái Customer.
     * Ví dụ ACTIVE hoặc INACTIVE.
     */
    @PutMapping("/{customerId}")
    public CustomerResponse updateCustomerStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request
    ) {
        return customerService.updateCustomerStatus(
                customerId,
                request.getCustomerStatus()
        );
    }

    /*
     * Xóa mềm Customer bằng cách chuyển sang INACTIVE.
     * Không xóa vật lý để giữ lại lịch sử thuê xe.
     */
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(
            @PathVariable Long customerId
    ) {
        customerService.updateCustomerStatus(
                customerId,
                CustomerStatus.INACTIVE
        );
    }
}