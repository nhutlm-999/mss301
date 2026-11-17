package fu.ce190737.enrollment.service;

import fu.ce190737.enrollment.dto.EnrollmentDTO;
import fu.ce190737.enrollment.dto.EnrollmentDetailDTO;
import fu.ce190737.enrollment.dto.PageDTO;

public interface EnrollmentService {
    EnrollmentDetailDTO getById(long id);

    PageDTO<EnrollmentDetailDTO> getAll(int page, int size, String studentName, String status);

    EnrollmentDTO create(EnrollmentDTO r);

    EnrollmentDTO update(long id, EnrollmentDTO r);

    void delete(long id);
}
