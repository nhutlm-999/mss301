package fu.ce190284.room.controller;

import fu.ce190284.room.dto.*;
import fu.ce190284.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class CE190284RoomController {
    private final RoomService service;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<RoomDTO>> create(@Valid @RequestBody RoomDTO d) {
        return ResponseEntity.status(201).body(ApiResponseDTO.success(service.create(d), "Room created successfully"));
    }

    @PutMapping("/{roomId}")
    public ApiResponseDTO<RoomDTO> update(@PathVariable Long roomId, @RequestBody @Valid RoomDTO d) {
        return ApiResponseDTO.success(service.update(roomId, d), "Room updated successfully");
    }

    @GetMapping("/{roomId}")
    public ApiResponseDTO<RoomDTO> detail(@PathVariable Long roomId) {
        return ApiResponseDTO.success(service.getById(roomId), "Successful");
    }

    @DeleteMapping("/{roomId}")
    public ApiResponseDTO<Void> delete(@PathVariable Long roomId) {
        service.delete(roomId);
        return ApiResponseDTO.success(null, "Room deleted successfully");
    }

    @GetMapping
    public ApiResponseDTO<PageDTO<RoomDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String roomNumber,
                                                 @RequestParam(required = false) String roomType,
                                                 @RequestParam(required = false) String status) {
        return ApiResponseDTO.success(service.getAll(page, size, roomNumber, roomType, status), "Successful");
    }
}
