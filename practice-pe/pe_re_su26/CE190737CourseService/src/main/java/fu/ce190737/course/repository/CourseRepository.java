package fu.ce190737.course.repository;

import fu.ce190737.course.entity.Course;
import fu.ce190737.course.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE " +
            "(:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<Course> search(@Param("title") String title,
                             @Param("status") CourseStatus status,
                             Pageable pageable);
}
