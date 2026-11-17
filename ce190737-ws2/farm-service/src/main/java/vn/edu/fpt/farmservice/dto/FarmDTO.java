package vn.edu.fpt.farmservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public record FarmDTO(
        Long farmId,
        String farmCode,
        String name,
        String location,
        Double area,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date establishedDate
) {
}
