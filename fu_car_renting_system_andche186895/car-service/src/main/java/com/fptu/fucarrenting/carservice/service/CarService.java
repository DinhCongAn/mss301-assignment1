package com.fptu.fucarrenting.carservice.service;

import com.fptu.fucarrenting.carservice.dto.CarResponse;
import com.fptu.fucarrenting.carservice.dto.CreateCarRequest;
import com.fptu.fucarrenting.carservice.entity.CarInformation;
import com.fptu.fucarrenting.carservice.entity.CarStatus;
import com.fptu.fucarrenting.carservice.entity.Manufacturer;
import com.fptu.fucarrenting.carservice.entity.Supplier;
import com.fptu.fucarrenting.carservice.repository.CarInformationRepository;
import com.fptu.fucarrenting.carservice.repository.ManufacturerRepository;
import com.fptu.fucarrenting.carservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.fptu.fucarrenting.carservice.dto.UpdateCarRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarInformationRepository carInformationRepository;

    private final ManufacturerRepository manufacturerRepository;

    private final SupplierRepository supplierRepository;

    @Transactional
    public CarResponse createCar(CreateCarRequest request) {

        Manufacturer manufacturer = manufacturerRepository
                .findById(request.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Manufacturer not found"
                ));

        Supplier supplier = supplierRepository
                .findById(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        CarInformation car = new CarInformation();

        car.setCarName(
                request.getCarName().trim()
        );

        car.setCarDescription(
                normalizeNullableText(
                        request.getCarDescription()
                )
        );

        car.setNumberOfDoors(
                request.getNumberOfDoors()
        );

        car.setSeatingCapacity(
                request.getSeatingCapacity()
        );

        car.setFuelType(
                request.getFuelType()
                        .trim()
                        .toUpperCase()
        );

        car.setYear(
                request.getYear()
        );

        car.setManufacturer(manufacturer);

        car.setSupplier(supplier);

        car.setCarStatus(
                request.getCarStatus()
        );

        car.setCarRentingPricePerDay(
                request.getCarRentingPricePerDay()
        );

        CarInformation savedCar =
                carInformationRepository.save(car);

        return toCarResponse(savedCar);
    }

    private CarResponse toCarResponse(
            CarInformation car
    ) {
        return new CarResponse(
                car.getCarId(),
                car.getCarName(),
                car.getCarDescription(),
                car.getNumberOfDoors(),
                car.getSeatingCapacity(),
                car.getFuelType(),
                car.getYear(),

                car.getManufacturer()
                        .getManufacturerId(),

                car.getManufacturer()
                        .getManufacturerName(),

                car.getSupplier()
                        .getSupplierId(),

                car.getSupplier()
                        .getSupplierName(),

                car.getCarStatus(),

                car.getCarRentingPricePerDay()
        );
    }

    private String normalizeNullableText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {

        return carInformationRepository
                .findAllByOrderByCarIdDesc()
                .stream()
                .map(this::toCarResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CarResponse getCarById(Long carId) {

        CarInformation car = carInformationRepository
                .findById(carId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found"
                ));

        return toCarResponse(car);
    }

    /*
     * Admin cập nhật toàn bộ thông tin của một xe.
     */
    @Transactional
    public CarResponse updateCar(
            Long carId,
            UpdateCarRequest request
    ) {
        /*
         * Kiểm tra xe cần cập nhật có tồn tại không.
         */
        CarInformation car = carInformationRepository
                .findById(carId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found"
                ));

        /*
         * Kiểm tra hãng sản xuất mới có tồn tại không.
         */
        Manufacturer manufacturer = manufacturerRepository
                .findById(request.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Manufacturer not found"
                ));

        /*
         * Kiểm tra nhà cung cấp mới có tồn tại không.
         */
        Supplier supplier = supplierRepository
                .findById(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        /*
         * Cập nhật thông tin cơ bản.
         */
        car.setCarName(
                request.getCarName().trim()
        );

        car.setCarDescription(
                normalizeNullableText(
                        request.getCarDescription()
                )
        );

        car.setNumberOfDoors(
                request.getNumberOfDoors()
        );

        car.setSeatingCapacity(
                request.getSeatingCapacity()
        );

        car.setFuelType(
                request.getFuelType()
                        .trim()
                        .toUpperCase()
        );

        car.setYear(
                request.getYear()
        );

        /*
         * Cập nhật quan hệ Manufacturer và Supplier.
         */
        car.setManufacturer(manufacturer);
        car.setSupplier(supplier);

        car.setCarStatus(
                request.getCarStatus()
        );

        car.setCarRentingPricePerDay(
                request.getCarRentingPricePerDay()
        );

        /*
         * Vì car đã được lấy từ database nên đang được
         * Hibernate quản lý. Gọi save giúp code rõ ràng hơn.
         */
        CarInformation updatedCar =
                carInformationRepository.save(car);

        return toCarResponse(updatedCar);
    }

    /*
     * Xóa xe theo hình thức logical delete.
     *
     * Không xóa bản ghi khỏi database vì Renting Service
     * có thể đang lưu carId trong lịch sử thuê.
     */
    @Transactional
    public void deleteCar(Long carId) {

        CarInformation car = carInformationRepository
                .findById(carId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found"
                ));

        /*
         * Nếu xe đã INACTIVE thì không cần thay đổi thêm.
         * Điều này giúp thao tác delete có tính idempotent.
         */
        if (car.getCarStatus() == CarStatus.INACTIVE) {
            return;
        }

        car.setCarStatus(CarStatus.INACTIVE);

        carInformationRepository.save(car);
    }

    /*
     * Lấy danh sách xe đang được phép cho thuê.
     *
     * Method này chỉ kiểm tra trạng thái hoạt động của xe,
     * chưa kiểm tra xe có bị trùng lịch thuê hay không.
     */
    @Transactional(readOnly = true)
    public List<CarResponse> getAvailableCars() {

        return carInformationRepository
                .findAllByCarStatusOrderByCarIdDesc(
                        CarStatus.AVAILABLE
                )
                .stream()
                .map(this::toCarResponse)
                .toList();
    }
}