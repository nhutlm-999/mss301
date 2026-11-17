package fu.ce190284.reservation.client;

import fu.ce190284.reservation.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// Doi client theo service goi den
@FeignClient(name = "room-service", url = "${room-service.url}")
public interface RoomClient {
    @GetMapping("/api/rooms/{roomId}")
    ApiResponseDTO<RoomDTO> getRoom(@PathVariable("roomId") Long roomId);
}
