package fu.ce190737.reservation.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int status;      // Mã status bên trong JSON ApiResponseDTO (0, 2, 3, 4, 5...)
    private final int httpCode;    // Mã HTTP Response (406, 226, 404, 400, 409, 500...)

    public ApiException(int status, int httpCode, String message) {
        super(message);
        this.status = status;
        this.httpCode = httpCode;
    }

    public ApiException(int status, HttpStatus http, String message) {
        this(status, http.value(), message);
    }

    // ==================== HÀM TÙY BIẾN VẠN NĂNG (Dùng khi đề có mã lạ) ====================
    // VD: throw ApiException.of(6, 409, "Conflict state");
    public static ApiException of(int apiStatus, int httpCode, String message) {
        return new ApiException(apiStatus, httpCode, message);
    }

    // ==================== 1. LỖI VALIDATION (HTTP 406 - Status 2) ====================
    public static ApiException invalid() {
        return new ApiException(2, 406, "Data validation failed");
    }

    public static ApiException invalid(String message) {
        return new ApiException(2, 406, message);
    }

    // ==================== 2. LỖI TRÙNG DỮ LIỆU UNIQUE (HTTP 226 - Status 3) ====================
    public static ApiException duplicate() {
        return new ApiException(3, 226, "Duplicate unique data");
    }

    public static ApiException duplicate(String message) {
        return new ApiException(3, 226, message);
    }

    // Phòng trường hợp đề khác yêu cầu trùng lặp trả về 409 CONFLICT thay vì 226
    public static ApiException conflict(String message) {
        return new ApiException(3, 409, message);
    }

    // ==================== 3. LỖI KHÔNG TÌM THẤY (HTTP 404 - Status 4) ====================
    public static ApiException missing() {
        return new ApiException(4, 404, "Resource is not found");
    }

    public static ApiException missing(String message) {
        return new ApiException(4, 404, message);
    }

    // ==================== 4. LỖI TRẠNG THÁI NGHIỆP VỤ (HTTP 400 - Status 5) ====================
    // VD: Phòng không AVAILABLE, sách hết hàng, đơn hàng đã hủy không thể sửa...
    public static ApiException badState() {
        return new ApiException(5, 400, "Resource is not available for this operation");
    }

    public static ApiException badState(String message) {
        return new ApiException(5, 400, message);
    }

    // Alias giữ tương thích với đề Hotel (RoomService / ReservationService)
    public static ApiException roomNotAvailable() {
        return badState("Room is not AVAILABLE for reservation");
    }

    // ==================== 5. CÁC LỖI PHỔ BIẾN KHÁC TRONG MICROSERVICES ====================
    // HTTP 401 - Chưa đăng nhập / Sai token
    public static ApiException unauthorized(String message) {
        return new ApiException(0, 401, message);
    }

    // HTTP 403 - Không có quyền truy cập
    public static ApiException forbidden(String message) {
        return new ApiException(0, 403, message);
    }

    // HTTP 422 - Dữ liệu đúng cú pháp nhưng sai logic nghiệp vụ
    public static ApiException unprocessable(String message) {
        return new ApiException(2, 422, message);
    }

    // HTTP 500 - Lỗi hệ thống hoặc gọi Feign sang Service khác bị chết
    public static ApiException internal(String message) {
        return new ApiException(0, 500, message);
    }
}