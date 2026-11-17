package fu.ce190737.course.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190737.course.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long courseId;
    String courseCode;
    String title;
    String description;
    Integer credits;
    @Enumerated(value = EnumType.STRING)
    CourseStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    Date endDate;
    Long instructorId;
}
