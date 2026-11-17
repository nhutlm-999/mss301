package fu.ce190737.course.service;

import fu.ce190737.course.common.ApiException;
import fu.ce190737.course.common.Check;
import fu.ce190737.course.dto.CourseDTO;
import fu.ce190737.course.dto.PageDTO;
import fu.ce190737.course.entity.Course;
import fu.ce190737.course.enums.CourseStatus;
import fu.ce190737.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService{
    final CourseRepository repo;

    private CourseDTO dto (Course c) {
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

    private Course save(Course c, CourseDTO d, boolean isCreate)
    {
        if (isCreate || d.courseId() != null){
            c.setCourseCode(Check.str(d.courseCode(), 1, 20));
        }
        if (isCreate || d.title() != null){
            c.setTitle(Check.str(d.title(), 100));
        }
        if (isCreate || d.description() != null){
            c.setDescription(Check.str(d.description(), 500));
        }
        if (isCreate || d.credits() != null){
            c.setCredits(Check.intRange(d.credits(),1, 10));
        }
        if (d.status() != null){
            c.setStatus(d.status());
        } else if (isCreate){
            c.setStatus(CourseStatus.OPEN);
        }
//

        if (isCreate || d.startDate() != null || d.endDate() != null) {
            Date in = d.startDate() != null ? d.startDate() : c.getStartDate();
            Date out = d.endDate() != null ? d.endDate() : c.getEndDate();
            Check.dateAfter(in, out);
            c.setStartDate(in);
            c.setEndDate(out);
        }

        if (isCreate || d.instructorId() != null){
            c.setInstructorId(Check.id(d.instructorId()));
        }
        return repo.save(c);
    }

    @Override
    public CourseDTO getById(long id) {
        Course c = repo.findById(id).orElseThrow(ApiException::missing);
        return dto(c);
    }

    @Override
    public PageDTO<CourseDTO> getAll(int page, int size, String title, String status) {
        Check.page(page, size);
        CourseStatus statusEnum = Check.enumParam(CourseStatus.class, status);
        String titleS = (title == null || title.isBlank()) ? null : title.trim();

//        Page<Reservation> reservationPage = this.repo.findByGuestNameContainingAndStatus(guestName, status, pageable);
        Page<Course> result = this.repo.search(title, statusEnum, PageRequest.of(page, size));
        return PageDTO.of(result.map((c) -> Check.map(c, CourseDTO.class)));
    }

    @Override
    public CourseDTO create(CourseDTO c) {
        return dto(save(new Course(), c, true));
    }

    @Override
    public CourseDTO update(long id, CourseDTO c) {
        Course course = repo.findById(id).orElseThrow(ApiException::missing);
           return dto(save(course, c, false));

    }

    @Override
    public void delete(long id) {

    }
}
