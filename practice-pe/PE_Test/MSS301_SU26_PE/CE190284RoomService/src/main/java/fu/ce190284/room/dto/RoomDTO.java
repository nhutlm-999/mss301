package fu.ce190284.room.dto;

import fu.ce190284.room.enums.RoomStatus;
import fu.ce190284.room.enums.RoomType;

import java.math.BigDecimal;

public record RoomDTO(
        Long roomId,
        String roomNumber,
        RoomType roomType,
        BigDecimal pricePerNight,
        Integer capacity,
        Integer floor,
        RoomStatus status
) {
}
