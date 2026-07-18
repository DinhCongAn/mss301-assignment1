package com.fptu.fucarrenting.carservice.service;

import com.fptu.fucarrenting.carservice.dto.ManufacturerRequest;
import com.fptu.fucarrenting.carservice.dto.ManufacturerResponse;
import com.fptu.fucarrenting.carservice.entity.Manufacturer;
import com.fptu.fucarrenting.carservice.repository.ManufacturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturerService {

    /*
     * Repository dùng để thao tác với bảng manufacturers
     * trong car_db.
     */
    private final ManufacturerRepository manufacturerRepository;

    /*
     * Tạo mới một hãng sản xuất.
     */
    @Transactional
    public ManufacturerResponse createManufacturer(
            ManufacturerRequest request
    ) {
        /*
         * Xóa khoảng trắng đầu và cuối tên hãng.
         *
         * Ví dụ:
         * "  Toyota  " sẽ trở thành "Toyota".
         */
        String manufacturerName =
                request.getManufacturerName().trim();

        /*
         * Kiểm tra tên hãng đã tồn tại hay chưa.
         *
         * IgnoreCase có nghĩa:
         * Toyota, TOYOTA và toyota được xem là giống nhau.
         */
        if (manufacturerRepository
                .existsByManufacturerNameIgnoreCase(
                        manufacturerName
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Manufacturer name already exists"
            );
        }

        /*
         * Chuyển DTO thành Entity.
         */
        Manufacturer manufacturer = new Manufacturer();

        manufacturer.setManufacturerName(
                manufacturerName
        );

        manufacturer.setDescription(
                normalizeNullableText(
                        request.getDescription()
                )
        );

        manufacturer.setManufacturerCountry(
                normalizeNullableText(
                        request.getManufacturerCountry()
                )
        );

        /*
         * Lưu dữ liệu vào bảng manufacturers.
         */
        Manufacturer savedManufacturer =
                manufacturerRepository.save(manufacturer);

        /*
         * Không trả trực tiếp Entity.
         * Chuyển Entity thành DTO response.
         */
        return toManufacturerResponse(savedManufacturer);
    }

    /*
     * Chuyển Manufacturer Entity thành ManufacturerResponse.
     */
    private ManufacturerResponse toManufacturerResponse(
            Manufacturer manufacturer
    ) {
        return new ManufacturerResponse(
                manufacturer.getManufacturerId(),
                manufacturer.getManufacturerName(),
                manufacturer.getDescription(),
                manufacturer.getManufacturerCountry()
        );
    }

    /*
     * Chuẩn hóa trường không bắt buộc.
     *
     * null       → null
     * ""         → null
     * "   "      → null
     * " Japan "  → "Japan"
     */
    private String normalizeNullableText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /*
     * Lấy toàn bộ hãng sản xuất.
     */
    @Transactional(readOnly = true)
    public List<ManufacturerResponse> getAllManufacturers() {

        return manufacturerRepository
                .findAll()
                .stream()
                .map(this::toManufacturerResponse)
                .toList();
    }
}