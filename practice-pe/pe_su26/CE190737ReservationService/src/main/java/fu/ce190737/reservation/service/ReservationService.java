package fu.ce190737.reservation.service;

import fu.ce190737.reservation.dto.PageDTO;
import fu.ce190737.reservation.dto.ReservationDetailDto;
import fu.ce190737.reservation.dto.ReservationDto;

public interface ReservationService {
    ReservationDetailDto getById(long id);

    PageDTO<ReservationDetailDto> getAll(int page, int size, String guestName, String status);

    ReservationDto create(ReservationDto r);

    ReservationDto update(long id, ReservationDto r);

    void delete(long id);
}
