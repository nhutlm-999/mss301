package vn.edu.fpt.carservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarDto {
    private Integer carId;
    private String carName;
    private String model;
    private String color;
    private BigDecimal pricePerDay;
    private String status;
}