package vn.edu.fpt.harvestservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public record HarvestDTO(
        Long harvestId,
        Long farmId,
        String farmCode,
        String farmName,
        String crop,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date harvestDate,
        Double weightKg
) {
}
