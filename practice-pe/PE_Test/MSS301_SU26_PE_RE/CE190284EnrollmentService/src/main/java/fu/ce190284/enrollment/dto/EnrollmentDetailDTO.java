package fu.ce190284.enrollment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190284.enrollment.enums.EnrollmentStatus;

import java.util.Date;

public record EnrollmentDetailDTO(
        Long enrollmentId,
        Long studentId,
        String studentName,
        String studentEmail,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd",
                timezone = "UTC"
        )
        Date enrollmentDate,

        EnrollmentStatus status,
        String grade,
        CourseDTO course
) {
}
