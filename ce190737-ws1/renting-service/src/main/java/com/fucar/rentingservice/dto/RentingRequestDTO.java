package com.fucar.rentingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentingRequestDTO {
    private String customerId;
    private Integer carId;
    private LocalDate rentStartDate;
    private LocalDate rentEndDate;
}
