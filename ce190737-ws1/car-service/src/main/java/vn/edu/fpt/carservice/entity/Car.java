package vn.edu.fpt.carservice.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Car")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarID")
    private Integer carId;

    @Column(name = "CarName", nullable = false, unique = true)
    private String carName;

    @Column(name = "Model")
    private String model;

    @Column(name = "Color")
    private String color;

    @Column(name = "PricePerDay")
    private BigDecimal pricePerDay;

    @Column(name = "Status")
    private String status; // Available, Rented
}