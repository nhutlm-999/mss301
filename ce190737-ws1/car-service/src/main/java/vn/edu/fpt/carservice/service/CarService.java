package vn.edu.fpt.carservice.service;

import vn.edu.fpt.carservice.dto.CarDto;
import java.util.List;

public interface CarService {
    CarDto createCar(CarDto carDTO);
    List<CarDto> getAllCars();
    CarDto getCarById(Integer id);
    CarDto updateCar(Integer id, CarDto carDTO);
    void deleteCar(Integer id);
}
