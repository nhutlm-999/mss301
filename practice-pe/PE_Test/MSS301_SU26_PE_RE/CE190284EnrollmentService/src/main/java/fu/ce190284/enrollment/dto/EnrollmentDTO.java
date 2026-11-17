
package fu.ce190284.enrollment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190284.enrollment.enums.EnrollmentStatus;
import jakarta.validation.constraints.*;

import java.util.Date;

public record EnrollmentDTO(
        Long enrollmentId,
        Long studentId,
        @Size(max = 100) String studentName,
        @Email @Size(max = 100) String studentEmail,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd",
                timezone = "UTC"
        )
        Date enrollmentDate,

        EnrollmentStatus status,

        @Pattern(regexp = "[ABCDF]")
        String grade,

        Long courseId
) {

}
