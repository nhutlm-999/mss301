package com.fucar.rentingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {
    private Integer carId;
    private String carName;
    private String model;
    private String color;
    private BigDecimal pricePerDay;
    private String status;
}
