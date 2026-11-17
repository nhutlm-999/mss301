package vn.edu.fpt.harvestservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(
        name = "harvest",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_harvest_farm_crop_date", columnNames = {"farm_id", "crop", "harvest_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Harvest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_id")
    private Long harvestId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "crop", length = 20, nullable = false)
    private String crop;

    @Column(name = "harvest_date", nullable = false)
    private Date harvestDate;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;
}
