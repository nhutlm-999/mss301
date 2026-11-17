package fu.ce190284.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190284.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationDetailDTO(
        Long reservationId,
        String guestName,
        String guestEmail,
        String guestPhone,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkInDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkOutDate,

        Integer numberOfGuests,
        BigDecimal totalAmount,
        ReservationStatus status,
        RoomDTO room
) {
}
