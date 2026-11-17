package fu.ce190737.reservation.common;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import fu.ce190737.reservation.dto.ApiResponseDTO;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt mọi lỗi chủ động ném ra từ ApiException (406/2, 226/3, 404/4, 400/5...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getHttpCode())
                .body(ApiResponseDTO.error(e.getStatus(), e.getMessage()));
    }

    // 2. Bắt TẤT CẢ các lỗi Validation, sai kiểu dữ liệu, sai Enum, sai format Date/JSON -> HTTP 406 (status = 2)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,       // Lỗi @Valid trên @RequestBody
            HandlerMethodValidationException.class,      // Lỗi validation trên parameter (Spring Boot 3.2+/4.x)
            ConstraintViolationException.class,          // Lỗi @NotNull, @Min, @Max trên @RequestParam / @PathVariable
            BindException.class,                         // Lỗi bind dữ liệu form/query
            HttpMessageNotReadableException.class,       // JSON sai cú pháp, sai tên Enum, sai format ngày tháng
            MethodArgumentTypeMismatchException.class,   // Truyền chữ vào chỗ cần số (VD: /api/rooms/abc, page=xyz)
            MissingServletRequestParameterException.class,// Thiếu query param bắt buộc
            MissingPathVariableException.class,          // Thiếu path variable
            DateTimeParseException.class,                // Lỗi parse ngày giờ
            IllegalArgumentException.class               // Lỗi Enum.valueOf() hoặc đối số bất hợp lệ
    })
    public ResponseEntity<ApiResponseDTO<Void>> handleValidationExceptions(Exception e) {
        // Nếu bên trong HttpMessageNotReadableException có nguyên nhân gốc là ApiException thì lấy theo ApiException
        if (e.getCause() instanceof ApiException apiEx) {
            return handleApiException(apiEx);
        }
        return ResponseEntity
                .status(406)
                .body(ApiResponseDTO.error(2, "Data validation failed"));
    }

    // 3. Bắt lỗi trùng khóa Unique hoặc vi phạm ràng buộc dưới SQL Server -> HTTP 226 (status = 3)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(226)
                .body(ApiResponseDTO.error(3, "Duplicate unique data or constraint violation"));
    }

    // 4. Bắt các lỗi không tìm thấy bản ghi hoặc sai đường dẫn -> HTTP 404 (status = 4)
    @ExceptionHandler({
            NoSuchElementException.class,
            EntityNotFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(Exception e) {
        return ResponseEntity
                .status(404)
                .body(ApiResponseDTO.error(4, "Resource is not found"));
    }

    // 5. Bắt lỗi sai HTTP Method (405) hoặc sai Content-Type (415)
    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodOrMediaNotSupported(Exception e) {
        return ResponseEntity
                .status(406)
                .body(ApiResponseDTO.error(2, e.getMessage()));
    }

    // 6. Chốt chặn cuối cùng: Tự động nhận diện lỗi OpenFeign (404 từ Service kia) hoặc trả về HTTP 500 (status = 0)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAllOtherExceptions(Exception e) {
        // Nếu gọi Feign sang Service khác mà nhận về 404 NotFound -> Tự động trả về 404 (status = 4)
        if (e.getClass().getSimpleName().contains("NotFound")) {
            return ResponseEntity
                    .status(404)
                    .body(ApiResponseDTO.error(4, "Referenced resource is not found"));
        }
        return ResponseEntity
                .status(500)
                .body(ApiResponseDTO.error(0, "Internal server error"));
    }
}