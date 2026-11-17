package fu.ce190737.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fu.ce190737.reservation.dto.ApiResponseDTO;
import fu.ce190737.reservation.dto.PageDTO;
import fu.ce190737.reservation.dto.ReservationDetailDto;
import fu.ce190737.reservation.dto.ReservationDto;
import fu.ce190737.reservation.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class CE190737ReservationController {
    private final ReservationService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReservationDto>> create(@Valid @RequestBody ReservationDto d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Reservation created successfully"));
    }

    @PutMapping("/{reservationId}")
    public ApiResponseDTO<ReservationDto> update(@PathVariable Long reservationId, @Valid @RequestBody ReservationDto d) {
        return ApiResponseDTO.success(service.update(reservationId, d), "Reservation updated successfully");
    }

    @GetMapping("/{reservationId}")
    public ApiResponseDTO<ReservationDetailDto> detail(@PathVariable Long reservationId) {
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
    public ApiResponseDTO<PageDTO<ReservationDetailDto>> list(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(required = false) String guestName,
                                                              @RequestParam(required = false) String status) {
        String searchName = (guestName != null && !guestName.trim().isEmpty()) ? guestName.trim().toLowerCase() : null;
        String searchStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toLowerCase() : null;
        return ApiResponseDTO.success(service.getAll(page, size, searchName, searchStatus), "Successful");
    }
}
