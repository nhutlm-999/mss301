package fu.ce190284.course.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int status;
    private final HttpStatus http;

    public ApiException(int s, HttpStatus h, String m) {
        super(m);
        status = s;
        http = h;
    }

    public static ApiException invalid() {
        return new ApiException(2, HttpStatus.NOT_ACCEPTABLE, "Data validation failed");
    }

    public static ApiException duplicate() {
        return new ApiException(3, HttpStatus.valueOf(226), "Course code is duplicated");
    }

    public static ApiException missing() {
        return new ApiException(4, HttpStatus.NOT_FOUND, "Course is not found");
    }
}
