package fu.ce190737.enrollment.repository;

import fu.ce190737.enrollment.entity.Enrollment;
import fu.ce190737.enrollment.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT r FROM Enrollment r WHERE " +
            "(:studentName IS NULL OR LOWER(r.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) AND " +
            "(:status IS NULL OR r.status = :status)")
    Page<Enrollment> search(@Param("studentName") String studentName,
                            @Param("status") EnrollmentStatus status,
                            Pageable pageable);
}
