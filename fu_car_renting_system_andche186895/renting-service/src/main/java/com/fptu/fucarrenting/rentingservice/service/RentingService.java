package com.fptu.fucarrenting.rentingservice.service;

import com.fptu.fucarrenting.rentingservice.client.CarClient;
import com.fptu.fucarrenting.rentingservice.client.dto.CarClientResponse;
import com.fptu.fucarrenting.rentingservice.dto.CreateRentingRequest;
import com.fptu.fucarrenting.rentingservice.dto.RentingItemRequest;
import com.fptu.fucarrenting.rentingservice.dto.RentingTransactionResponse;
import com.fptu.fucarrenting.rentingservice.entity.RentingStatus;
import com.fptu.fucarrenting.rentingservice.repository.RentingDetailRepository;
import com.fptu.fucarrenting.rentingservice.service.model.CalculatedRentingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fptu.fucarrenting.rentingservice.client.CustomerClient;
import com.fptu.fucarrenting.rentingservice.client.dto.CustomerEligibilityResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentingService {

    private final CarClient carClient;
    private final RentingDetailRepository rentingDetailRepository;
    private final CustomerClient customerClient;


    public List<CalculatedRentingItem> validateAndCalculateItems(
            CreateRentingRequest request
    ) {

        validateDuplicateCars(request);

        return request.getDetails()
                .stream()
                .map(this::validateAndCalculateItem)
                .toList();
    }

    private CalculatedRentingItem validateAndCalculateItem(
            RentingItemRequest item
    ) {
        validateRentalDates(
                item.getStartDate(),
                item.getEndDate()
        );

        CarClientResponse car =
                carClient.getCarById(item.getCarId());

        if (!"AVAILABLE".equalsIgnoreCase(
                car.getCarStatus()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car " + item.getCarId()
                            + " is not available for renting"
            );
        }

        if (car.getCarRentingPricePerDay() == null
                || car.getCarRentingPricePerDay()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car " + item.getCarId()
                            + " has an invalid renting price"
            );
        }

        long overlappingCount =
                rentingDetailRepository
                        .countOverlappingRentals(
                                item.getCarId(),
                                item.getStartDate(),
                                item.getEndDate(),
                                List.of(
                                        RentingStatus.PENDING,
                                        RentingStatus.EXECUTING,
                                        RentingStatus.COMPLETED,
                                        RentingStatus.COMPENSATING
                                )
                        );

        if (overlappingCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car " + item.getCarId()
                            + " is already rented "
                            + "during the requested period"
            );
        }

        long rentalDays =
                ChronoUnit.DAYS.between(
                        item.getStartDate(),
                        item.getEndDate()
                ) + 1;

        BigDecimal detailPrice =
                car.getCarRentingPricePerDay()
                        .multiply(
                                BigDecimal.valueOf(rentalDays)
                        );

        return new CalculatedRentingItem(
                item.getCarId(),
                item.getStartDate(),
                item.getEndDate(),
                detailPrice
        );
    }

    private void validateRentalDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date must not be in the past"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date must not be before start date"
            );
        }
    }

    private void validateDuplicateCars(
            CreateRentingRequest request
    ) {
        Set<Long> carIds = new HashSet<>();

        for (RentingItemRequest detail
                : request.getDetails()) {

            if (!carIds.add(detail.getCarId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Car " + detail.getCarId()
                                + " appears more than once "
                                + "in the renting transaction"
                );
            }
        }
    }

    public void validateCustomerEligibility(
            Long customerId
    ) {

        if (customerId == null || customerId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer ID is invalid"
            );
        }

        CustomerEligibilityResponse eligibility =
                customerClient.checkEligibility(customerId);

        if (eligibility == null
                || eligibility.getCustomerId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Customer Service returned an invalid response"
            );
        }

        if (!customerId.equals(
                eligibility.getCustomerId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Customer Service returned mismatched customer data"
            );
        }

        if (!eligibility.isEligible()) {

            String message = eligibility.getMessage();

            if (message == null || message.isBlank()) {
                message = "Customer is not eligible to rent a car";
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    message
            );
        }
    }
}