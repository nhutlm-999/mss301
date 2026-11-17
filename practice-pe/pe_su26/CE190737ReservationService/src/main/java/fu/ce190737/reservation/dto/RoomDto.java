package fu.ce190737.reservation.dto;

import fu.ce190737.reservation.enums.RoomStatus;
import fu.ce190737.reservation.enums.RoomType;

import java.math.BigDecimal;

public record RoomDto(Long roomId, String roomNumber,
                      RoomType roomType,
                      BigDecimal pricePerNight,
                      int capacity,
                      int floor,
                      RoomStatus status) {
}

