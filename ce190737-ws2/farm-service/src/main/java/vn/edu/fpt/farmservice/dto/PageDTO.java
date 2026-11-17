package vn.edu.fpt.farmservice.dto;

public record PageDTO(int size, int page, int totalPages, long totalElements, boolean first, boolean last, Object content) {
}
