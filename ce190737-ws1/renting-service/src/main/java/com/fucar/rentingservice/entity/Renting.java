package com.fucar.rentingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Renting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentingID")
    private Integer rentingId;

    @Column(name = "CustomerId")
    private String customerId;

    @Column(name = "CarId")
    private Integer carId;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "TotalPrice")
    private BigDecimal totalPrice;
}
