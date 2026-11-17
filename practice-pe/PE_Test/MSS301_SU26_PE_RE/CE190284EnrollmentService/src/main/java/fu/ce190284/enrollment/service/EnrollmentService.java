package fu.ce190284.enrollment.service;

import fu.ce190284.enrollment.dto.*;

public interface EnrollmentService {
    EnrollmentDTO create(EnrollmentDTO d);

    EnrollmentDTO update(Long id, EnrollmentDTO d);

    EnrollmentDetailDTO detail(Long id);

    void cancel(Long id);

    PageDTO<EnrollmentDetailDTO> list(int page, int size, String studentName, String status);
}
