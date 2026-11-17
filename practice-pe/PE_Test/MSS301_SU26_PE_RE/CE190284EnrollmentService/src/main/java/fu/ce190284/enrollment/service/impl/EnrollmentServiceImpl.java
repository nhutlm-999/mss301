
package fu.ce190284.enrollment.service.impl;

import feign.FeignException;
import fu.ce190284.enrollment.client.CourseClient;
import fu.ce190284.enrollment.common.ApiException;
import fu.ce190284.enrollment.dto.ApiResponseDTO;
import fu.ce190284.enrollment.dto.CourseDTO;
import fu.ce190284.enrollment.dto.EnrollmentDTO;
import fu.ce190284.enrollment.dto.EnrollmentDetailDTO;
import fu.ce190284.enrollment.dto.PageDTO;
import fu.ce190284.enrollment.entity.Enrollment;
import fu.ce190284.enrollment.enums.CourseStatus;
import fu.ce190284.enrollment.enums.EnrollmentStatus;
import fu.ce190284.enrollment.repository.EnrollmentRepository;
import fu.ce190284.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository repo;
    private final CourseClient courseClient;

    // Create a new enrollment
    @Override
    @Transactional
    public EnrollmentDTO create(EnrollmentDTO d) {

        // Validate student information
        if (d.studentId() == null
                || d.studentId() <= 0
                || blank(d.studentName())
                || d.studentName().length() > 100
                || blank(d.studentEmail())
                || d.studentEmail().length() > 100
                || !d.studentEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                || d.enrollmentDate() == null
                || d.courseId() == null
                || d.courseId() <= 0
                || !validGrade(d.grade())) {

            throw ApiException.invalid();
        }

        // Check course existence and status using Feign
        CourseDTO course = getCourse(d.courseId());

        if (course.status() != CourseStatus.OPEN) {
            throw ApiException.courseClosed();
        }

        // Create enrollment with default ACTIVE status
        Enrollment e = Enrollment.builder()
                .studentId(d.studentId())
                .studentName(d.studentName())
                .studentEmail(d.studentEmail())
                .enrollmentDate(d.enrollmentDate())
                .status(EnrollmentStatus.ACTIVE)
                .grade(d.grade())
                .courseId(d.courseId())
                .build();

        return dto(repo.save(e));
    }

    // Update an existing enrollment
    @Override
    @Transactional
    public EnrollmentDTO update(Long id, EnrollmentDTO d) {

        Enrollment e = repo.findById(id)
                .orElseThrow(ApiException::missing);

        // Update student ID
        if (d.studentId() != null) {

            if (d.studentId() <= 0) {
                throw ApiException.invalid();
            }

            e.setStudentId(d.studentId());
        }

        // Update student name
        if (d.studentName() != null) {

            if (blank(d.studentName())
                    || d.studentName().length() > 100) {

                throw ApiException.invalid();
            }

            e.setStudentName(d.studentName());
        }

        // Update student email
        if (d.studentEmail() != null) {

            if (blank(d.studentEmail())
                    || d.studentEmail().length() > 100
                    || !d.studentEmail().matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                throw ApiException.invalid();
            }

            e.setStudentEmail(d.studentEmail());
        }

        // Update enrollment date
        if (d.enrollmentDate() != null) {
            e.setEnrollmentDate(d.enrollmentDate());
        }

        // Update enrollment status
        if (d.status() != null) {
            e.setStatus(d.status());
        }

        // Update grade
        if (d.grade() != null) {

            if (!validGrade(d.grade())) {
                throw ApiException.invalid();
            }

            e.setGrade(d.grade());
        }

        // Update course ID
        if (d.courseId() != null) {

            if (d.courseId() <= 0) {
                throw ApiException.invalid();
            }

            // Check course existence and status
            CourseDTO course = getCourse(d.courseId());

            if (course.status() != CourseStatus.OPEN) {
                throw ApiException.courseClosed();
            }

            e.setCourseId(d.courseId());
        }

        return dto(repo.save(e));
    }

    // Get enrollment details with course information
    @Override
    @Transactional(readOnly = true)
    public EnrollmentDetailDTO detail(Long id) {

        Enrollment e = repo.findById(id)
                .orElseThrow(ApiException::missing);

        CourseDTO course = getCourse(e.getCourseId());

        return detailDto(e, course);
    }

    // Cancel an enrollment
    @Override
    @Transactional
    public void cancel(Long id) {

        Enrollment e = repo.findById(id)
                .orElseThrow(ApiException::missing);

        e.setStatus(EnrollmentStatus.CANCELLED);

        repo.save(e);
    }

    // Get paginated enrollments with optional name and status filters
    @Override
    @Transactional(readOnly = true)
    public PageDTO<EnrollmentDetailDTO> list(
            int page, int size, String studentName, String status) {

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.invalid();
        }

        // Convert String to EnrollmentStatus Enum
        EnrollmentStatus enrollmentStatus = parseStatus(status);

        Pageable p = PageRequest.of(
                page,
                size,
                Sort.by("enrollmentId")
        );

        boolean hasName = studentName != null
                && !studentName.isBlank();

        String name = hasName ? studentName.trim() : "";

        Page<Enrollment> result;

        // Filter by name and status
        if (hasName && enrollmentStatus != null) {

            result = repo.findByStudentNameContainingIgnoreCaseAndStatus(
                    name, enrollmentStatus, p);

            // Filter by name only
        } else if (hasName) {

            result = repo.findByStudentNameContainingIgnoreCase(
                    name, p);

            // Filter by status only
        } else if (enrollmentStatus != null) {

            result = repo.findByStatus(enrollmentStatus, p);

            // Get all enrollments
        } else {

            result = repo.findAll(p);
        }

        // Cache course information to avoid repeated Feign calls
        Map<Long, CourseDTO> cache = new HashMap<>();

        return PageDTO.of(
                result.map(e -> detailDto(
                        e,
                        cache.computeIfAbsent(
                                e.getCourseId(),
                                this::getCourse
                        )
                ))
        );
    }

    // Convert String to EnrollmentStatus Enum
    private EnrollmentStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return EnrollmentStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    // Get course information from Course Service using Feign
    private CourseDTO getCourse(Long id) {

        try {
            ApiResponseDTO<CourseDTO> response =
                    courseClient.getCourse(id);

            if (response == null || response.data() == null) {
                throw ApiException.missing();
            }

            return response.data();

        } catch (FeignException.NotFound e) {

            throw ApiException.missing();

        } catch (FeignException e) {

            throw new IllegalStateException(
                    "Course Service is unavailable", e);
        }
    }

    // Check whether a string is null or empty
    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    // Check whether grade is valid
    private boolean validGrade(String grade) {
        return grade == null || grade.matches("[ABCDF]");
    }

    // Convert Enrollment entity to EnrollmentDTO
    private EnrollmentDTO dto(Enrollment e) {

        return new EnrollmentDTO(
                e.getEnrollmentId(),
                e.getStudentId(),
                e.getStudentName(),
                e.getStudentEmail(),
                e.getEnrollmentDate(),
                e.getStatus(),
                e.getGrade(),
                e.getCourseId()
        );
    }

    // Convert Enrollment entity and CourseDTO to EnrollmentDetailDTO
    private EnrollmentDetailDTO detailDto(
            Enrollment e, CourseDTO course) {

        return new EnrollmentDetailDTO(
                e.getEnrollmentId(),
                e.getStudentId(),
                e.getStudentName(),
                e.getStudentEmail(),
                e.getEnrollmentDate(),
                e.getStatus(),
                e.getGrade(),
                course
        );
    }
}
