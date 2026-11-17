package fu.ce190284.enrollment.entity;

import fu.ce190284.enrollment.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    @Column(name = "student_email", nullable = false, length = 100)
    private String studentEmail;

    @Temporal(TemporalType.DATE)
    @Column(name = "enrollment_date", nullable = false)
    private Date enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private EnrollmentStatus status;

    @Column(name = "grade", length = 5)
    private String grade;

    @Column(name = "course_id", nullable = false)
    private Long courseId;
}
