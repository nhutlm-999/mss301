
package fu.ce190284.course.service.impl;

import fu.ce190284.course.common.ApiException;
import fu.ce190284.course.dto.CourseDTO;
import fu.ce190284.course.dto.PageDTO;
import fu.ce190284.course.entity.Course;
import fu.ce190284.course.enums.CourseStatus;
import fu.ce190284.course.repository.CourseRepository;
import fu.ce190284.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repo;

    // Create a new course
    @Override
    @Transactional
    public CourseDTO create(CourseDTO d) {

        // Validate required fields
        if (d.courseCode() == null
                || !d.courseCode().matches("[A-Za-z0-9]{1,20}")
                || d.title() == null
                || d.title().isBlank()
                || d.title().length() > 150
                || d.credits() == null
                || d.credits() < 1
                || d.credits() > 10) {

            throw ApiException.invalid();
        }

        // Validate course dates
        validateDates(d.startDate(), d.endDate());

        // Check duplicate course code
        if (repo.existsByCourseCode(d.courseCode())) {
            throw ApiException.duplicate();
        }

        // Create course with default DRAFT status
        Course c = Course.builder()
                .courseCode(d.courseCode())
                .title(d.title())
                .description(d.description())
                .credits(d.credits())
                .status(d.status() == null
                        ? CourseStatus.DRAFT
                        : d.status())
                .startDate(d.startDate())
                .endDate(d.endDate())
                .instructorId(d.instructorId())
                .build();

        return dto(repo.save(c));
    }

    // Update an existing course
    @Override
    @Transactional
    public CourseDTO update(Long id, CourseDTO d) {

        Course c = repo.findById(id)
                .orElseThrow(ApiException::missing);

        // Update course code
        if (d.courseCode() != null) {

            if (d.courseCode().isBlank()
                    || !d.courseCode().matches("[A-Za-z0-9]{1,20}")) {

                throw ApiException.invalid();
            }

            // Check duplicate course code
            if (repo.existsByCourseCodeAndCourseIdNot(
                    d.courseCode(), id)) {

                throw ApiException.duplicate();
            }

            c.setCourseCode(d.courseCode());
        }

        // Update course title
        if (d.title() != null) {

            if (d.title().isBlank()
                    || d.title().length() > 150) {

                throw ApiException.invalid();
            }

            c.setTitle(d.title());
        }

        // Update course description
        if (d.description() != null) {
            c.setDescription(d.description());
        }

        // Update course credits
        if (d.credits() != null) {

            if (d.credits() < 1 || d.credits() > 10) {
                throw ApiException.invalid();
            }

            c.setCredits(d.credits());
        }

        // Update course status
        if (d.status() != null) {
            c.setStatus(d.status());
        }

        // Get new dates or keep existing dates
        Date start = d.startDate() != null
                ? d.startDate()
                : c.getStartDate();

        Date end = d.endDate() != null
                ? d.endDate()
                : c.getEndDate();

        // Validate dates if either date changes
        if (d.startDate() != null || d.endDate() != null) {
            validateDates(start, end);
        }

        c.setStartDate(start);
        c.setEndDate(end);

        // Update instructor ID
        if (d.instructorId() != null) {
            c.setInstructorId(d.instructorId());
        }

        return dto(repo.save(c));
    }

    // Get course by ID
    @Override
    @Transactional(readOnly = true)
    public CourseDTO getById(Long id) {

        Course c = repo.findById(id)
                .orElseThrow(ApiException::missing);

        return dto(c);
    }

    // Close a course
    @Override
    @Transactional
    public void close(Long id) {

        Course c = repo.findById(id)
                .orElseThrow(ApiException::missing);

        c.setStatus(CourseStatus.CLOSED);

        repo.save(c);
    }

    // Get paginated courses with optional title and status filters
    @Override
    @Transactional(readOnly = true)
    public PageDTO<CourseDTO> getAll(
            int page, int size, String title, String status) {

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.invalid();
        }

        // Convert String to CourseStatus Enum
        CourseStatus courseStatus = parseStatus(status);

        Pageable p = PageRequest.of(
                page,
                size,
                Sort.by("courseId")
        );

        String t = title == null ? "" : title.trim();

        Page<Course> result;

        // Filter by title and status
        if (courseStatus != null) {

            result = repo.findByTitleContainingIgnoreCaseAndStatus(
                    t, courseStatus, p);

            // Filter by title only
        } else if (title != null && !title.isBlank()) {

            result = repo.findByTitleContainingIgnoreCase(t, p);

            // Get all courses
        } else {

            result = repo.findAll(p);
        }

        return PageDTO.of(result.map(this::dto));
    }

    // Convert String to CourseStatus Enum
    private CourseStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return CourseStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    // Validate course start and end dates
    private void validateDates(Date start, Date end) {

        if (start == null
                || end == null
                || !end.after(start)
                || !start.after(new Date())) {

            throw ApiException.invalid();
        }
    }

    // Convert Course entity to CourseDTO
    private CourseDTO dto(Course c) {

        return new CourseDTO(
                c.getCourseId(),
                c.getCourseCode(),
                c.getTitle(),
                c.getDescription(),
                c.getCredits(),
                c.getStatus(),
                c.getStartDate(),
                c.getEndDate(),
                c.getInstructorId()
        );
    }
}
