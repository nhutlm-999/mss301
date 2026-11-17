package fu.ce190737.reservation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import fu.ce190737.reservation.dto.ApiResponseDTO;
import fu.ce190737.reservation.dto.RoomDto;

@FeignClient(name = "room-service", url = "http://localhost:8081")
public interface RoomClient {
    @GetMapping("/api/rooms/{roomId}")
    ApiResponseDTO<RoomDto> getRoom(@PathVariable("roomId") Long roomId);
}