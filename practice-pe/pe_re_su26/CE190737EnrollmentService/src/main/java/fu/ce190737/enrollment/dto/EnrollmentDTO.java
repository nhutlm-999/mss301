package fu.ce190737.enrollment.dto;

import fu.ce190737.enrollment.enums.EnrollmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Date;

public record EnrollmentDTO(
        Long enrollmentId,
        Long studentId,
        String studentName,
        String studentEmail,
        Date enrollmentDate,
        @Enumerated(value = EnumType.STRING)
        EnrollmentStatus status,
        String grade,
        Long courseId
) {
}
