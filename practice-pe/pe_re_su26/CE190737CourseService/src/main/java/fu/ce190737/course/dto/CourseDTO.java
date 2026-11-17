package fu.ce190737.course.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190737.course.enums.CourseStatus;
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
        CourseStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date endDate,
        Long instructorId
) {
}
