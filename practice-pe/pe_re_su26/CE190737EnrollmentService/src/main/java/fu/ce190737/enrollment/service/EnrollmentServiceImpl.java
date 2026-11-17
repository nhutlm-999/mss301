package fu.ce190737.enrollment.service;

import fu.ce190737.enrollment.client.CourseClient;
import fu.ce190737.enrollment.common.ApiException;
import fu.ce190737.enrollment.common.Check;
import fu.ce190737.enrollment.dto.*;
import fu.ce190737.enrollment.entity.Enrollment;
import fu.ce190737.enrollment.enums.CourseStatus;
import fu.ce190737.enrollment.enums.EnrollmentStatus;
import fu.ce190737.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService{
    final private EnrollmentRepository repo;
    private final CourseClient client;
    private EnrollmentDTO dto (Enrollment e) {
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
    private EnrollmentDetailDTO dtoDetail (Enrollment e, CourseDTO c) {
        return new EnrollmentDetailDTO(
                e.getEnrollmentId(),
                e.getStudentId(),
                e.getStudentName(),
                e.getStudentEmail(),
                e.getEnrollmentDate(),
                e.getStatus(),
                e.getGrade(),
                c
        );
    }

    private Enrollment save(Enrollment c, EnrollmentDTO d, boolean isCreate)
    {
        if (isCreate || d.studentId() != null){
            c.setStudentId(Check.id(d.studentId()));
        }
        if (isCreate || d.studentName() != null){
            c.setStudentName(Check.str(d.studentName(), 100));
        }
        if (isCreate || d.studentEmail() != null){
            c.setStudentEmail(Check.email(d.studentEmail(), 500));
        }
        if (isCreate || d.enrollmentDate() != null){
            c.setEnrollmentDate(Check.notNull(d.enrollmentDate()));
        }
        if (d.status() != null){
            c.setStatus(d.status());
        } else if (isCreate){
            c.setStatus(EnrollmentStatus.ACTIVE);
        }
        if (isCreate || d.grade() != null){
            c.setGrade(Check.str(d.grade(), 0, 100));
        }
        if (isCreate || d.courseId() != null){
            Check.id(d.courseId());
            CourseDTO courseDTO = client.get(d.courseId()).data();
            if (courseDTO == null) {
                throw ApiException.missing();
            }
            if (courseDTO.status() != CourseStatus.OPEN) {
                throw ApiException.badState("Course is not active");
            }
            c.setCourseId(Check.id(d.courseId()));
        }
        return repo.save(c);
    }

    @Override
    public EnrollmentDetailDTO getById(long id) {
        return null;
    }

    @Override
    public PageDTO<EnrollmentDetailDTO> getAll(int page, int size, String studentName, String status) {
        Check.page(page, size);
        EnrollmentStatus statusEnum = Check.enumParam(EnrollmentStatus.class, status);
        String nameFilter = (studentName == null || studentName.isBlank()) ? null : studentName.trim();

//        Page<Reservation> reservationPage = this.repo.findByGuestNameContainingAndStatus(guestName, status, pageable);
        Page<Enrollment> reservationPage = this.repo.search(nameFilter, statusEnum, PageRequest.of(page, size));
        return PageDTO.of(reservationPage.map((reservation) -> {
            ApiResponseDTO<CourseDTO> roomResponse = client.get(reservation.getCourseId());
            CourseDTO room = (roomResponse != null) ? roomResponse.data() : null;
            return dtoDetail(reservation, room);
        }));
    }

    @Override
    public EnrollmentDTO create(EnrollmentDTO r) {
        return dto(save(new Enrollment(), r, true));
    }

    @Override
    public EnrollmentDTO update(long id, EnrollmentDTO r) {
        return null;
    }

    @Override
    public void delete(long id) {

    }
}
