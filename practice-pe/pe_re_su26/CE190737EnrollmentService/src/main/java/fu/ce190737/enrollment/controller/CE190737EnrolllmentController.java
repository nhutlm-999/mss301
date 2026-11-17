package fu.ce190737.enrollment.controller;

import fu.ce190737.enrollment.dto.ApiResponseDTO;
import fu.ce190737.enrollment.dto.EnrollmentDTO;
import fu.ce190737.enrollment.dto.EnrollmentDetailDTO;
import fu.ce190737.enrollment.dto.PageDTO;
import fu.ce190737.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments"
)
@RequiredArgsConstructor
public class CE190737EnrolllmentController {
    private final EnrollmentService service;
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EnrollmentDTO>> create(@Valid @RequestBody EnrollmentDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Reservation created successfully"));
    }

    @PutMapping("/{reservationId}")
    public ApiResponseDTO<EnrollmentDTO> update(@PathVariable Long reservationId, @Valid @RequestBody EnrollmentDTO d) {
        return ApiResponseDTO.success(service.update(reservationId, d), "Reservation updated successfully");
    }

    @GetMapping("/{reservationId}")
    public ApiResponseDTO<EnrollmentDetailDTO> detail(@PathVariable Long reservationId) {
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
    public ApiResponseDTO<PageDTO<EnrollmentDetailDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(required = false) String studentName,
                                                             @RequestParam(required = false) String status) {
        String searchName = (studentName != null && !studentName.trim().isEmpty()) ? studentName.trim().toLowerCase() : null;
        String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toLowerCase() : null;
        return ApiResponseDTO.success(service.getAll(page, size, searchName, searchStatus), "Successful");
    }
}
