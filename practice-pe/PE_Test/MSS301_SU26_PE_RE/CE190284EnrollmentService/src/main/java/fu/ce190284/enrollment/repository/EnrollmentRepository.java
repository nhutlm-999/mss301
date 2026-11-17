package fu.ce190284.enrollment.repository;

import fu.ce190284.enrollment.entity.Enrollment;
import fu.ce190284.enrollment.enums.EnrollmentStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByStudentNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Enrollment> findByStudentNameContainingIgnoreCaseAndStatus(String name, EnrollmentStatus status, Pageable pageable);

    Page<Enrollment> findByStatus(EnrollmentStatus status, Pageable pageable);
}
