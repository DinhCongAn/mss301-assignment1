package com.fptu.fucarrenting.customerservice.service;

import com.fptu.fucarrenting.customerservice.dto.*;
import com.fptu.fucarrenting.customerservice.entity.Customer;
import com.fptu.fucarrenting.customerservice.entity.CustomerStatus;
import com.fptu.fucarrenting.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        Customer customer = new Customer();

        customer.setCustomerName(
                request.getCustomerName().trim()
        );

        customer.setTelephone(
                request.getTelephone().trim()
        );

        customer.setEmail(email);

        customer.setCustomerBirthday(
                request.getCustomerBirthday()
        );

        customer.setCustomerStatus(
                CustomerStatus.ACTIVE
        );

        customer.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        return new RegisterResponse(
                savedCustomer.getCustomerId(),
                savedCustomer.getCustomerName(),
                savedCustomer.getTelephone(),
                savedCustomer.getEmail(),
                savedCustomer.getCustomerBirthday(),
                savedCustomer.getCustomerStatus()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        /*
         * Kiểm tra tài khoản Admin được cấu hình
         * trong application.yml.
         */
        if (email.equalsIgnoreCase(adminEmail.trim())
                && request.getPassword().equals(adminPassword)) {

            String accessToken = jwtService.generateToken(
                    null,
                    adminEmail,
                    "ADMIN"
            );

            return new LoginResponse(
                    accessToken,
                    "Bearer",
                    "ADMIN",
                    null,
                    adminEmail
            );
        }

        /*
         * Nếu không phải Admin thì tìm Customer trong database.
         */
        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        /*
         * So sánh mật khẩu người dùng nhập
         * với mật khẩu BCrypt trong database.
         */
        if (!passwordEncoder.matches(
                request.getPassword(),
                customer.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        /*
         * Chỉ Customer có trạng thái ACTIVE mới được đăng nhập.
         */
        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Customer account is not active"
            );
        }

        String accessToken = jwtService.generateToken(
                customer.getCustomerId(),
                customer.getEmail(),
                "CUSTOMER"
        );

        return new LoginResponse(
                accessToken,
                "Bearer",
                "CUSTOMER",
                customer.getCustomerId(),
                customer.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile(Long customerId) {

        Customer customer = findCustomerById(customerId);

        return toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateMyProfile(
            Long customerId,
            UpdateProfileRequest request
    ) {
        Customer customer = findCustomerById(customerId);

        customer.setCustomerName(
                request.getCustomerName().trim()
        );

        customer.setTelephone(
                request.getTelephone().trim()
        );

        customer.setCustomerBirthday(
                request.getCustomerBirthday()
        );

        Customer updatedCustomer =
                customerRepository.save(customer);

        return toCustomerResponse(updatedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {

        Customer customer = findCustomerById(customerId);

        return toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomerStatus(
            Long customerId,
            CustomerStatus customerStatus
    ) {
        Customer customer = findCustomerById(customerId);

        customer.setCustomerStatus(customerStatus);

        Customer updatedCustomer =
                customerRepository.save(customer);

        return toCustomerResponse(updatedCustomer);
    }

    private Customer findCustomerById(Long customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));
    }

    private CustomerResponse toCustomerResponse(Customer customer) {

        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getTelephone(),
                customer.getEmail(),
                customer.getCustomerBirthday(),
                customer.getCustomerStatus()
        );
    }

    @Transactional(readOnly = true)
    public CustomerEligibilityResponse checkEligibility(Long customerId) {

        Customer customer = findCustomerById(customerId);

        boolean eligible =
                customer.getCustomerStatus() == CustomerStatus.ACTIVE;

        String message;

        if (eligible) {
            message = "Customer is eligible to rent a car";
        } else {
            message = "Customer account is not active";
        }

        return new CustomerEligibilityResponse(
                customer.getCustomerId(),
                eligible,
                customer.getCustomerStatus(),
                message
        );
    }
}