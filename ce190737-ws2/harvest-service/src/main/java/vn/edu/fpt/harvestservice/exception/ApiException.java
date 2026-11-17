package vn.edu.fpt.harvestservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int errorCode;
    private final HttpStatus httpStatus;

    public ApiException(int errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static ApiException invalidData(String message) {
        return new ApiException(2, HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException duplicate(String message) {
        return new ApiException(3, HttpStatus.CONFLICT, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(4, HttpStatus.NOT_FOUND, message);
    }

    public static ApiException businessRuleViolation(String message) {
        return new ApiException(5, HttpStatus.CONFLICT, message);
    }
}
