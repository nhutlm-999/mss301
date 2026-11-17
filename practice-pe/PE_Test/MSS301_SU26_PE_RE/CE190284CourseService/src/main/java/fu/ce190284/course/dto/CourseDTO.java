package fu.ce190284.course.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190284.course.enums.CourseStatus;

import java.util.Date;

public record CourseDTO(
        Long courseId,
        String courseCode,
        String title,
        String description,
        Integer credits,
        CourseStatus status,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd",
                timezone = "UTC"
        )
        Date startDate,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd",
                timezone = "UTC"
        )
        Date endDate,

        Long instructorId
) {
}