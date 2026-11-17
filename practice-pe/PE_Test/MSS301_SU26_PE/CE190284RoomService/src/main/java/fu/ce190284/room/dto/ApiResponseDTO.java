package fu.ce190284.room.dto;

import java.time.*;
import java.time.format.DateTimeFormatter;

public record ApiResponseDTO<T>(int status, T data, String timestamp, String message) {
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return new ApiResponseDTO<>(1, data, now(), message);
    }

    public static <T> ApiResponseDTO<T> error(int status, String message) {
        return new ApiResponseDTO<>(status, null, now(), message);
    }

    private static String now() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC).format(Instant.now());
    }
}
