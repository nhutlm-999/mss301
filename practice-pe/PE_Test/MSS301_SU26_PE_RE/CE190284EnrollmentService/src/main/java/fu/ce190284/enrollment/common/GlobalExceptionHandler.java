package fu.ce190284.enrollment.common;

import fu.ce190284.enrollment.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDTO<Void>> api(ApiException e) {
        return ResponseEntity.status(e.getHttp()).body(ApiResponseDTO.error(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponseDTO<Void>> invalid(Exception e) {
        return ResponseEntity.status(406).body(ApiResponseDTO.error(2, "Data validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> other(Exception e) {
        return ResponseEntity.status(500).body(ApiResponseDTO.error(0, "Internal server error"));
    }
}
