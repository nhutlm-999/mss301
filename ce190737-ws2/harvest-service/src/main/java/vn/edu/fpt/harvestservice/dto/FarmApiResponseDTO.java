package vn.edu.fpt.harvestservice.dto;

public record FarmApiResponseDTO(
        int status,
        FarmDTO data,
        String timestamp
) {
}
