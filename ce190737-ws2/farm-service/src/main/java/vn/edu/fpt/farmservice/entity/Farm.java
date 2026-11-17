package vn.edu.fpt.farmservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "farm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_id")
    private Long farmId;

    @Column(name = "farm_code", length = 20, nullable = false, unique = true)
    private String farmCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "location", length = 100, nullable = false)
    private String location;

    @Column(name = "area", nullable = false)
    private double area;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "established_date", nullable = false)
    private Date establishedDate;
}
