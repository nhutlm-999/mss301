package vn.edu.fpt.harvestservice.dto;

public record ApiResponseDTO(
        int status,
        Object data,
        String timestamp
) {
}
