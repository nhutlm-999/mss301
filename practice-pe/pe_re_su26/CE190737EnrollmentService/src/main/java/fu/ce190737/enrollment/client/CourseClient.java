package fu.ce190737.enrollment.client;

import fu.ce190737.enrollment.dto.ApiResponseDTO;
import fu.ce190737.enrollment.dto.CourseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "http://localhost:8081")
public interface CourseClient {
    @GetMapping("/api/courses/{id}")
    ApiResponseDTO<CourseDTO> get(@PathVariable("id") Long id);
}