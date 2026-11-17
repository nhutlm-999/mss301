package vn.edu.fpt.carservice.service.impl;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.edu.fpt.carservice.dto.CarDto;
import vn.edu.fpt.carservice.exception.CustomException;
import vn.edu.fpt.carservice.entity.Car;
import vn.edu.fpt.carservice.repository.CarRepository;
import vn.edu.fpt.carservice.service.CarService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {
    private CarRepository carRepository;

    CarServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    private CarDto mapToDTO(Car car) {
        return CarDto.builder()
                .carId(car.getCarId())
                .carName(car.getCarName())
                .model(car.getModel())
                .color(car.getColor())
                .pricePerDay(car.getPricePerDay())
                .status(car.getStatus())
                .build();
    }

    private void validateCarInput(CarDto dto) {
        if (dto.getCarName() == null || dto.getCarName().isBlank() ||
                dto.getModel() == null || dto.getModel().isBlank() ||
                dto.getColor() == null || dto.getColor().isBlank() ||
                dto.getPricePerDay() == null || dto.getPricePerDay().compareTo(BigDecimal.ZERO) <= 0 ||
                dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new CustomException("Invalid data", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public CarDto createCar(CarDto carDTO) {
        validateCarInput(carDTO);
        Car car = Car.builder()
                .carName(carDTO.getCarName())
                .model(carDTO.getModel())
                .color(carDTO.getColor())
                .pricePerDay(carDTO.getPricePerDay())
                .status(carDTO.getStatus())
                .build();
        return mapToDTO(carRepository.save(car));
    }

    @Override
    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public CarDto getCarById(Integer id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CustomException("Car not found", HttpStatus.NOT_FOUND));
        return mapToDTO(car);
    }

    @Override
    public CarDto updateCar(Integer id, CarDto carDTO) {
        validateCarInput(carDTO);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CustomException("Car not found", HttpStatus.NOT_FOUND));

        car.setCarName(carDTO.getCarName());
        car.setModel(carDTO.getModel());
        car.setColor(carDTO.getColor());
        car.setPricePerDay(carDTO.getPricePerDay());
        car.setStatus(carDTO.getStatus());

        return mapToDTO(carRepository.save(car));
    }

    @Override
    public void deleteCar(Integer id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CustomException("Car not found", HttpStatus.NOT_FOUND));

        if ("Rented".equalsIgnoreCase(car.getStatus())) {
            throw new CustomException("Car is rented", HttpStatus.CONFLICT);
        }

        carRepository.delete(car);
    }
}