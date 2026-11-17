package fu.ce190284.course.controller;

import fu.ce190284.course.dto.*;
import fu.ce190284.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CE190284CourseController {
    private final CourseService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CourseDTO>> create(@Valid @RequestBody CourseDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Course created successfully"));
    }

    @PutMapping("/{courseId}")
    public ApiResponseDTO<CourseDTO> update(@PathVariable Long courseId, @RequestBody @Valid CourseDTO d) {
        return ApiResponseDTO.success(service.update(courseId, d), "Course updated successfully");
    }

    @GetMapping("/{courseId}")
    public ApiResponseDTO<CourseDTO> detail(@PathVariable Long courseId) {
        return ApiResponseDTO.success(service.getById(courseId), "Successful");
    }

    @DeleteMapping("/{courseId}")
    public ApiResponseDTO<Void> close(@PathVariable Long courseId) {
        service.close(courseId);
        return ApiResponseDTO.success(null, "Course closed successfully");
    }

    @GetMapping
    public ApiResponseDTO<PageDTO<CourseDTO>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String title, @RequestParam(required = false) String status) {
        return ApiResponseDTO.success(service.getAll(page, size, title, status), "Successful");
    }
}
