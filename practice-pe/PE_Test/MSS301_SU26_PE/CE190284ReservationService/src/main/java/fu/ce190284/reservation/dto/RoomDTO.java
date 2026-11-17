package fu.ce190284.reservation.dto;

import fu.ce190284.reservation.enums.RoomStatus;
import fu.ce190284.reservation.enums.RoomType;

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
