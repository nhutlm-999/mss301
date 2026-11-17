package vn.edu.fpt.harvestservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import vn.edu.fpt.harvestservice.dto.ApiResponseDTO;
import vn.edu.fpt.harvestservice.util.DateTimeUtils;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDTO> handleApiException(ApiException ex) {
        ApiResponseDTO response = new ApiResponseDTO(
                ex.getErrorCode(),
                null,
                DateTimeUtils.nowIsoUtc()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponseDTO> handleBadRequestException(Exception ex) {
        ApiResponseDTO response = new ApiResponseDTO(
                2,
                null,
                DateTimeUtils.nowIsoUtc()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO> handleGenericException(Exception ex) {
        ApiResponseDTO response = new ApiResponseDTO(
                0,
                null,
                DateTimeUtils.nowIsoUtc()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
