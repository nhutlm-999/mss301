package fu.ce190284.reservation.controller;

import fu.ce190284.reservation.dto.*;
import fu.ce190284.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class CE190284ReservationController {
    private final ReservationService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReservationDTO>> create(@Valid @RequestBody ReservationDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Reservation created successfully"));
    }

    @PutMapping("/{reservationId}")
    public ApiResponseDTO<ReservationDTO> update(@PathVariable Long reservationId, @Valid @RequestBody ReservationDTO d) {
        return ApiResponseDTO.success(service.update(reservationId, d), "Reservation updated successfully");
    }

    @GetMapping("/{reservationId}")
    public ApiResponseDTO<ReservationDetailDTO> detail(@PathVariable Long reservationId) {
        return ApiResponseDTO.success(service.detail(reservationId), "Successful");
    }

    @DeleteMapping("/{reservationId}")
    public ApiResponseDTO<Void> cancel(@PathVariable Long reservationId) {
        service.cancel(reservationId);
        return ApiResponseDTO.success(null, "Reservation cancelled successfully");
    }

    @GetMapping
    public ApiResponseDTO<PageDTO<ReservationDetailDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(required = false) String guestName,
                                                              @RequestParam(required = false) String status) {
        return ApiResponseDTO.success(service.list(page, size, guestName, status), "Successful");
    }
}
