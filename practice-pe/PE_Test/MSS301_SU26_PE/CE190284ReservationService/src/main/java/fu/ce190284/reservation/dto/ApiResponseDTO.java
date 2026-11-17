package fu.ce190284.reservation.dto;

import java.time.*;
import java.time.format.DateTimeFormatter;

public record ApiResponseDTO<T>(int status, T data, String timestamp, String message) {

    public static <T> ApiResponseDTO<T> success(T d, String m) {
        return new ApiResponseDTO<>(1, d, now(), m);
    }

    public static <T> ApiResponseDTO<T> error(int s, String m) {
        return new ApiResponseDTO<>(s, null, now(), m);
    }

    private static String now() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC).format(Instant.now());
    }
}
