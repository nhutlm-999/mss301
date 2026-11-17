package fu.ce190284.enrollment.controller;

import fu.ce190284.enrollment.dto.*;
import fu.ce190284.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CE190284EnrollmentController {
    private final EnrollmentService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<EnrollmentDTO>> create(@Valid @RequestBody EnrollmentDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Enrollment created successfully"));
    }

    @PutMapping("/{enrollmentId}")
    public ApiResponseDTO<EnrollmentDTO> update(@PathVariable Long enrollmentId, @Valid @RequestBody EnrollmentDTO d) {
        return ApiResponseDTO.success(service.update(enrollmentId, d), "Enrollment updated successfully");
    }

    @GetMapping("/{enrollmentId}")
    public ApiResponseDTO<EnrollmentDetailDTO> detail(@PathVariable Long enrollmentId) {
        return ApiResponseDTO.success(service.detail(enrollmentId), "Successful");
    }

    @DeleteMapping("/{enrollmentId}")
    public ApiResponseDTO<Void> cancel(@PathVariable Long enrollmentId) {
        service.cancel(enrollmentId);
        return ApiResponseDTO.success(null, "Enrollment cancelled successfully");
    }

    @GetMapping
    public ApiResponseDTO<PageDTO<EnrollmentDetailDTO>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String studentName, @RequestParam(required = false) String status) {
        return ApiResponseDTO.success(service.list(page, size, studentName, status), "Successful");
    }
}
