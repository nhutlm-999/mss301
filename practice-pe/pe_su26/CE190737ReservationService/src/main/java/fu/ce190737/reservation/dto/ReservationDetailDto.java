package fu.ce190737.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190737.reservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.util.Date;

public record ReservationDetailDto(
        Long reservationId,
        String guestName,
        String guestEmail,
        String guestPhone,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date checkInDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        Date checkOutDate,
        Integer numberOfGuests,
        BigDecimal totalAmount,
        ReservationStatus status,
        RoomDto room
) {
}
