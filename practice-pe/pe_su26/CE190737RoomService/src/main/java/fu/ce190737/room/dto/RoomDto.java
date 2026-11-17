package fu.ce190737.room.dto;

import java.math.BigDecimal;

public record RoomDto(Long roomId,
                      String roomNumber,
                      RoomType roomType,
                      BigDecimal pricePerNight,
                      Integer capacity,
                      Integer floor,
                      RoomStatus status) {
}

