package fu.ce190284.course.service;

import fu.ce190284.course.dto.*;

public interface CourseService {
    CourseDTO create(CourseDTO d);

    CourseDTO update(Long id, CourseDTO d);

    CourseDTO getById(Long id);

    void close(Long id);

    PageDTO<CourseDTO> getAll(int page, int size, String title, String status);
}
