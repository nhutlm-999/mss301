package fu.ce190284.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.ce190284.reservation.enums.ReservationStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationDTO(
        Long reservationId,

        @Size(max = 100) String guestName,
        @Email @Size(max = 100) String guestEmail,
        @Size(max = 20) String guestPhone,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkInDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate checkOutDate,

        Integer numberOfGuests,
        BigDecimal totalAmount,
        ReservationStatus status,
        Long roomId
) {
}
