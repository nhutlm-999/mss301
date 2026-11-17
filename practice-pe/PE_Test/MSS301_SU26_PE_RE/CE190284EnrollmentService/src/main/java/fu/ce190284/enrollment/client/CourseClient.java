package fu.ce190284.enrollment.client;

import fu.ce190284.enrollment.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// Doi client theo service goi den
@FeignClient(name = "course-service", url = "${course-service.url}")
public interface CourseClient {
    @GetMapping("/api/courses/{courseId}")
    ApiResponseDTO<CourseDTO> getCourse(@PathVariable Long courseId);
}
