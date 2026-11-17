package fu.ce190737.room.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fu.ce190737.room.dto.ApiResponseDTO;
import fu.ce190737.room.dto.PageDTO;
import fu.ce190737.room.dto.RoomDto;
import fu.ce190737.room.service.RoomService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/rooms")
@AllArgsConstructor
public class CE190737RoomController {
    private final RoomService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<RoomDto>> create(@Valid @RequestBody RoomDto d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Room created successfully"));
    }

    @PutMapping("/{reservationId}")
    public ApiResponseDTO<RoomDto> update(@PathVariable Long reservationId, @Valid @RequestBody RoomDto d) {
        return ApiResponseDTO.success(service.update(reservationId, d), "Reservation updated successfully");
    }

    @GetMapping("/{reservationId}")
    public ApiResponseDTO<RoomDto> detail(@PathVariable Long reservationId) {
        return ApiResponseDTO.success(service.getById(reservationId), "Successful");
    }

    //
    @DeleteMapping("/{reservationId}")
    public ApiResponseDTO<Void> cancel(@PathVariable Long reservationId) {
        service.delete(reservationId);
        return ApiResponseDTO.success(null, "Reservation cancelled successfully");
    }

    @GetMapping
    public ApiResponseDTO<PageDTO<RoomDto>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String roomType,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String roomNumber) {
        return ApiResponseDTO.success(service.getAll(page, size, roomType, status, roomNumber), "Successful");
    }
}
