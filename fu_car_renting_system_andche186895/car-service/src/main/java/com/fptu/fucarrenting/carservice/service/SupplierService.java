package com.fptu.fucarrenting.carservice.service;

import com.fptu.fucarrenting.carservice.dto.SupplierRequest;
import com.fptu.fucarrenting.carservice.dto.SupplierResponse;
import com.fptu.fucarrenting.carservice.entity.Supplier;
import com.fptu.fucarrenting.carservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse createSupplier(
            SupplierRequest request
    ) {

        String supplierName =
                request.getSupplierName().trim();

        if (supplierRepository
                .existsBySupplierNameIgnoreCase(supplierName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Supplier name already exists"
            );
        }

        Supplier supplier = new Supplier();

        supplier.setSupplierName(supplierName);

        supplier.setSupplierDescription(
                normalizeNullableText(
                        request.getSupplierDescription()
                )
        );

        supplier.setSupplierAddress(
                normalizeNullableText(
                        request.getSupplierAddress()
                )
        );

        Supplier savedSupplier =
                supplierRepository.save(supplier);

        return toSupplierResponse(savedSupplier);
    }

    private SupplierResponse toSupplierResponse(
            Supplier supplier
    ) {
        return new SupplierResponse(
                supplier.getSupplierId(),
                supplier.getSupplierName(),
                supplier.getSupplierDescription(),
                supplier.getSupplierAddress()
        );
    }

    private String normalizeNullableText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /*
     * Lấy toàn bộ nhà cung cấp.
     */
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {

        return supplierRepository
                .findAll()
                .stream()
                .map(this::toSupplierResponse)
                .toList();
    }
}