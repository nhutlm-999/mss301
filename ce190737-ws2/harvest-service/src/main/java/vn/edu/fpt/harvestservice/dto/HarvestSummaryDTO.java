package vn.edu.fpt.harvestservice.dto;

import java.util.List;

public record HarvestSummaryDTO(
        Long farmId,
        String farmCode,
        String farmName,
        double totalWeightKg,
        int harvestCount,
        List<CropWeightDTO> byCrop
) {
}
