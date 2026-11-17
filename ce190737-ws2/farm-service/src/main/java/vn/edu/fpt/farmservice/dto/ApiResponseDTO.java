package vn.edu.fpt.farmservice.dto;

public record ApiResponseDTO(
        int status,
        Object data,
        String timestamp
) {
}
