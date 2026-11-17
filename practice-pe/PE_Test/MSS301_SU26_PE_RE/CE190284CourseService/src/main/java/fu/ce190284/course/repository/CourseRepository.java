package fu.ce190284.course.repository;

import fu.ce190284.course.entity.Course;
import fu.ce190284.course.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCourseCode(String code);

    boolean existsByCourseCodeAndCourseIdNot(String code, Long id);

    Page<Course> findByTitleContainingIgnoreCaseAndStatus(String title, CourseStatus status, Pageable p);

    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable p);

    Page<Course> findByStatus(CourseStatus status, Pageable p);
}
