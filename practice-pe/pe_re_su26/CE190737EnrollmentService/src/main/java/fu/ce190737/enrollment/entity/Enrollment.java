package fu.ce190737.enrollment.entity;

import fu.ce190737.enrollment.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor@RequiredArgsConstructor
@Table(name="enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long enrollmentId;
    Long studentId;
    String studentName;
    String studentEmail;
    Date enrollmentDate;
    @Enumerated(value = EnumType.STRING)
    EnrollmentStatus status;
    String grade;
    Long courseId;
}
