package fu.ce190737.course.controller;

import fu.ce190737.course.dto.ApiResponseDTO;
import fu.ce190737.course.dto.CourseDTO;
import fu.ce190737.course.dto.PageDTO;
import fu.ce190737.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CE190737CourseController {
    private final CourseService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CourseDTO>> create(@Valid @RequestBody CourseDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Reservation created successfully"));
    }

    @PutMapping("/{reservationId}")
    public ApiResponseDTO<CourseDTO> update(@PathVariable Long reservationId, @Valid @RequestBody CourseDTO d) {
        return ApiResponseDTO.success(service.update(reservationId, d), "Reservation updated successfully");
    }

    @GetMapping("/{reservationId}")
    public ApiResponseDTO<CourseDTO> detail(@PathVariable Long reservationId) {
        return ApiResponseDTO.success(service.getById(reservationId), "Successful");
    }

    //
    @DeleteMapping("/{reservationId}")
    public ApiResponseDTO<Void> cancel(@PathVariable Long reservationId) {
        service.delete(reservationId);
        return ApiResponseDTO.success(null, "Reservation cancelled successfully");
    }

    //
    @GetMapping
    public ApiResponseDTO<PageDTO<CourseDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String status) {
        String searchName = (title != null && !title.trim().isEmpty()) ? title.trim().toLowerCase() : null;
        String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toLowerCase() : null;
        return ApiResponseDTO.success(service.getAll(page, size, searchName, searchStatus), "Successful");
    }
}
