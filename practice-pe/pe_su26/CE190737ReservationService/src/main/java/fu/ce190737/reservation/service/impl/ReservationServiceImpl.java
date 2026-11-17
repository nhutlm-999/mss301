package fu.ce190737.reservation.service.impl;

import fu.ce190737.reservation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import fu.ce190737.reservation.client.RoomClient;
import fu.ce190737.reservation.common.ApiException;
import fu.ce190737.reservation.common.Check;
import fu.ce190737.reservation.entity.Reservation;
import fu.ce190737.reservation.entity.ReservationStatus;
import fu.ce190737.reservation.enums.RoomStatus;
import fu.ce190737.reservation.repository.ReservationRepository;
import fu.ce190737.reservation.service.ReservationService;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final RoomClient roomClient;
    private final ReservationRepository repo;


    @Override
    public ReservationDetailDto getById(long id) {
        Reservation r = repo.findById(id).orElseThrow(ApiException::missing);
        RoomDto room = roomClient.getRoom(r.getRoomId()).data();
        return detailDto(r, room);
    }

    @Override
    public PageDTO<ReservationDetailDto> getAll(int page, int size, String guestName, String status) {
        Check.page(page, size);
        ReservationStatus statusEnum = Check.enumParam(ReservationStatus.class, status);
        String nameFilter = (guestName == null || guestName.isBlank()) ? null : guestName.trim();

//        Page<Reservation> reservationPage = this.repo.findByGuestNameContainingAndStatus(guestName, status, pageable);
        Page<Reservation> reservationPage = this.repo.search(nameFilter, statusEnum, PageRequest.of(page, size));
        return PageDTO.of(reservationPage.map((reservation) -> {
            ApiResponseDTO<RoomDto> roomResponse = roomClient.getRoom(reservation.getRoomId());
            RoomDto room = (roomResponse != null) ? roomResponse.data() : null;
            return detailDto(reservation, room);
        }));
    }

    @Override
    public ReservationDto create(ReservationDto r) {
        Check.dateAfter(r.checkInDate(), r.checkOutDate());
        Long roomId = Check.id(r.roomId());
        RoomDto room = roomClient.getRoom(roomId).data();
        if (room == null) {
            throw ApiException.missing();
        }
        if (room.status() != RoomStatus.AVAILABLE) {
            throw ApiException.roomNotAvailable();
        }

        return dto(repo.save(Reservation.builder()
                .roomId(Check.id(r.roomId()))
//                .roomId(r.roomId())
                .status(Check.notNull(r.status()))
                .reservationId(r.reservationId())
                .guestName(Check.str(r.guestName(), 30))
                .guestEmail(Check.email(r.guestEmail(), 50))
                .guestPhone(Check.phone(r.guestPhone(), 11))
                .checkInDate(Check.notNull(r.checkInDate()))
                .checkOutDate(r.checkOutDate())
                .numberOfGuests(Check.intMin(r.numberOfGuests(), 1))
                .totalAmount(r.totalAmount())
                .build()));
    }

    @Override
    public ReservationDto update(long id, ReservationDto d) {
        Reservation r = repo.findById(id).orElseThrow(ApiException::missing);

        if (d.guestName() != null)
            r.setGuestName(Check.str(d.guestName(), 100));
        if (d.guestEmail() != null)
            r.setGuestEmail(Check.email(d.guestEmail(), 100));
        if (d.guestPhone() != null)
            r.setGuestPhone(Check.str(d.guestPhone(), 20));
        if (d.numberOfGuests() != null)
            r.setNumberOfGuests(Check.intRange(d.numberOfGuests(), 1, 10));
        if (d.totalAmount() != null)
            r.setTotalAmount(Check.decimalMinZero(d.totalAmount()));
        if (d.status() != null) r.setStatus(d.status());

        if (d.checkInDate() != null || d.checkOutDate() != null) {
            Date in = d.checkInDate() != null ? d.checkInDate() : r.getCheckInDate();
            Date out = d.checkOutDate() != null ? d.checkOutDate() : r.getCheckOutDate();
            Check.dateAfter(in, out);
            r.setCheckInDate(in);
            r.setCheckOutDate(out);
        }

        if (d.roomId() != null) {
            RoomDto room = roomClient.getRoom(Check.id(d.roomId())).data();
            if (room.status() != RoomStatus.AVAILABLE)
                throw ApiException.roomNotAvailable();
            r.setRoomId(d.roomId());
        }

        return dto(repo.save(r));
    }

    @Override
    public void delete(long id) {
        Reservation r = this.repo.findById(id).orElseThrow(ApiException::missing);
        Check.notNull(r);

        r.setStatus(ReservationStatus.CANCELLED);
        this.repo.save(r);
    }

    private ReservationDto dto(Reservation r) {
        return new ReservationDto(
                r.getReservationId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getNumberOfGuests(),
                r.getTotalAmount(),
                r.getStatus(),
                r.getRoomId()
        );
    }


    private ReservationDetailDto detailDto(Reservation r, RoomDto room) {

        return new ReservationDetailDto(
                r.getReservationId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getNumberOfGuests(),
                r.getTotalAmount(),
                r.getStatus(),
                room
        );
    }
}
