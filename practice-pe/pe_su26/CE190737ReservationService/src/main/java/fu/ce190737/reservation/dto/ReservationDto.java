package fu.ce190737.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import fu.ce190737.reservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.util.Date;

public record ReservationDto(
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
        @Enumerated(EnumType.STRING)
        ReservationStatus status,
        Long roomId
) {
}
