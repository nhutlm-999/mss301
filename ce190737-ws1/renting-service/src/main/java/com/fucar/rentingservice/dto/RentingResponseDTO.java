package com.fucar.rentingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentingResponseDTO {
    private Integer rentingId;
    private String customerId;
    private Integer carId;
    private String carName;
    private String carModel;
    private LocalDate rentStartDate;
    private LocalDate rentEndDate;
    private BigDecimal totalPrice;
}
