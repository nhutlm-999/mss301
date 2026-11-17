package fu.ce190737.enrollment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190737.enrollment.enums.CourseStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Date;

public record CourseDTO(
        Long courseId,
        String courseCode,
        String title,
        String description,
        Integer credits,
        @Enumerated(value = EnumType.STRING)
        fu.ce190737.enrollment.enums.CourseStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date endDate,
        Long instructorId
) {
}
