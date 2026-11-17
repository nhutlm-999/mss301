package com.fucar.rentingservice.service.impl;

import com.fucar.rentingservice.client.CarClient;
import com.fucar.rentingservice.dto.CarDTO;
import com.fucar.rentingservice.dto.RentingRequestDTO;
import com.fucar.rentingservice.dto.RentingResponseDTO;
import com.fucar.rentingservice.entity.Renting;
import com.fucar.rentingservice.exception.CustomException;
import com.fucar.rentingservice.repository.RentingRepository;
import com.fucar.rentingservice.service.RentingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentingServiceImpl implements RentingService {

    private RentingRepository rentingRepository;
    private CarClient carClient;

    RentingServiceImpl(RentingRepository rentingRepository, CarClient carClient) {
        this.rentingRepository = rentingRepository;
        this.carClient = carClient;
    }

    @Override
    public RentingResponseDTO createRenting(RentingRequestDTO request) {
        if (request == null ||
                request.getCustomerId() == null || request.getCustomerId().isBlank() ||
                request.getCarId() == null ||
                request.getRentStartDate() == null || request.getRentEndDate() == null ||
                !request.getRentEndDate().isAfter(request.getRentStartDate())) {
            throw new CustomException("Invalid data", HttpStatus.BAD_REQUEST);
        }

        CarDTO car = carClient.getCarById(request.getCarId());

        if ("Rented".equalsIgnoreCase(car.getStatus())) {
            throw new CustomException("Car is rented", HttpStatus.CONFLICT);
        }

        long days = ChronoUnit.DAYS.between(request.getRentStartDate(), request.getRentEndDate());
        BigDecimal totalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Renting renting = Renting.builder()
                .customerId(request.getCustomerId())
                .carId(request.getCarId())
                .startDate(request.getRentStartDate())
                .endDate(request.getRentEndDate())
                .totalPrice(totalPrice)
                .build();
        renting = rentingRepository.save(renting);

        car.setStatus("Rented");
        carClient.updateCar(car.getCarId(), car);

        return RentingResponseDTO.builder()
                .rentingId(renting.getRentingId())
                .customerId(renting.getCustomerId())
                .carId(car.getCarId())
                .carName(car.getCarName())
                .carModel(car.getModel())
                .rentStartDate(renting.getStartDate())
                .rentEndDate(renting.getEndDate())
                .totalPrice(renting.getTotalPrice())
                .build();
    }

    @Override
    public List<RentingResponseDTO> getAllRentings() {
        return rentingRepository.findAll().stream().map(renting -> {
            CarDTO car = carClient.getCarById(renting.getCarId());
            return RentingResponseDTO.builder()
                    .rentingId(renting.getRentingId())
                    .customerId(renting.getCustomerId())
                    .carId(renting.getCarId())
                    .carName(car != null ? car.getCarName() : null)
                    .carModel(car != null ? car.getModel() : null)
                    .rentStartDate(renting.getStartDate())
                    .rentEndDate(renting.getEndDate())
                    .totalPrice(renting.getTotalPrice())
                    .build();
        }).collect(Collectors.toList());
    }
}
