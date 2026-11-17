package com.fucar.rentingservice.client;

import com.fucar.rentingservice.dto.CarDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "car-service", url = "http://localhost:8081")
public interface CarClient {

    @GetMapping("/api/cars/{id}")
    CarDTO getCarById(@PathVariable("id") Integer id);

    @PutMapping("/api/cars/{id}")
    CarDTO updateCar(@PathVariable("id") Integer id, @RequestBody CarDTO carDTO);
}
