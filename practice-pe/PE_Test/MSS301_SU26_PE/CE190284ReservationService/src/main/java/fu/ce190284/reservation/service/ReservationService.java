package fu.ce190284.reservation.service;

import fu.ce190284.reservation.dto.*;

public interface ReservationService {
    ReservationDTO create(ReservationDTO d);

    ReservationDTO update(Long id, ReservationDTO d);

    ReservationDetailDTO detail(Long id);

    void cancel(Long id);

    PageDTO<ReservationDetailDTO> list(int page, int size, String guestName, String status);
}
