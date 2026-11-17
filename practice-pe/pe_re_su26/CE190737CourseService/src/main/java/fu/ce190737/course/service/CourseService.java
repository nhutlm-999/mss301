package fu.ce190737.course.service;

import fu.ce190737.course.dto.CourseDTO;
import fu.ce190737.course.dto.PageDTO;

public interface CourseService
{
    CourseDTO getById(long id);

    PageDTO<CourseDTO> getAll(int page, int size, String title, String status);

    CourseDTO create(CourseDTO r);

    CourseDTO update(long id, CourseDTO r);

    void delete(long id);
}
