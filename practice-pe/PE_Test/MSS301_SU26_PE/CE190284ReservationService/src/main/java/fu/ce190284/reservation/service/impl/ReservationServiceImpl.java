package fu.ce190284.reservation.service.impl;

import feign.FeignException;
import fu.ce190284.reservation.client.RoomClient;
import fu.ce190284.reservation.common.ApiException;
import fu.ce190284.reservation.dto.*;
import fu.ce190284.reservation.entity.Reservation;
import fu.ce190284.reservation.enums.ReservationStatus;
import fu.ce190284.reservation.enums.RoomStatus;
import fu.ce190284.reservation.repository.ReservationRepository;
import fu.ce190284.reservation.service.ReservationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final ReservationRepository repo;
    private final RoomClient roomClient;

    // Create a new reservation
    @Override
    @Transactional
    public ReservationDTO create(ReservationDTO d) {

        // Validate guest information and booking data
        if (blank(d.guestName())
                || d.guestName().length() > 100
                || blank(d.guestEmail())
                || d.guestEmail().length() > 100
                || !d.guestEmail().matches(EMAIL_REGEX)
                || blank(d.guestPhone())
                || d.guestPhone().length() > 20
                || d.checkInDate() == null
                || d.checkOutDate() == null
                || !d.checkOutDate().isAfter(d.checkInDate())
                || d.numberOfGuests() == null
                || d.numberOfGuests() < 1
                || (d.totalAmount() != null && d.totalAmount().signum() < 0)
                || d.roomId() == null
                || d.roomId() <= 0) {

            throw ApiException.invalid();
        }

        // Check room existence and status using Feign
        RoomDTO room = getRoom(d.roomId());

        if (room.status() != RoomStatus.AVAILABLE) {
            throw ApiException.roomNotAvailable();
        }

        // If client does not send totalAmount, compute = nights * pricePerNight
        BigDecimal total = d.totalAmount() != null
                ? d.totalAmount()
                : calcTotal(d.checkInDate(), d.checkOutDate(), room);

        // Create reservation with default CONFIRMED status
        Reservation r = Reservation.builder()
                .guestName(d.guestName())
                .guestEmail(d.guestEmail())
                .guestPhone(d.guestPhone())
                .checkInDate(d.checkInDate())
                .checkOutDate(d.checkOutDate())
                .numberOfGuests(d.numberOfGuests())
                .totalAmount(total)
                .status(ReservationStatus.CONFIRMED)
                .roomId(d.roomId())
                .build();

        return dto(repo.save(r));
    }

    // Update an existing reservation
    @Override
    @Transactional
    public ReservationDTO update(Long id, ReservationDTO d) {

        Reservation r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        // Update guest name
        if (d.guestName() != null) {

            if (blank(d.guestName()) || d.guestName().length() > 100) {
                throw ApiException.invalid();
            }

            r.setGuestName(d.guestName());
        }

        // Update guest email
        if (d.guestEmail() != null) {

            if (blank(d.guestEmail())
                    || d.guestEmail().length() > 100
                    || !d.guestEmail().matches(EMAIL_REGEX)) {

                throw ApiException.invalid();
            }

            r.setGuestEmail(d.guestEmail());
        }

        // Update guest phone
        if (d.guestPhone() != null) {

            if (blank(d.guestPhone()) || d.guestPhone().length() > 20) {
                throw ApiException.invalid();
            }

            r.setGuestPhone(d.guestPhone());
        }

        // Get new dates or keep existing dates, then validate
        LocalDate in = d.checkInDate() != null ? d.checkInDate() : r.getCheckInDate();
        LocalDate out = d.checkOutDate() != null ? d.checkOutDate() : r.getCheckOutDate();

        if (d.checkInDate() != null || d.checkOutDate() != null) {

            if (!out.isAfter(in)) {
                throw ApiException.invalid();
            }

            r.setCheckInDate(in);
            r.setCheckOutDate(out);
        }

        // Update number of guests
        if (d.numberOfGuests() != null) {

            if (d.numberOfGuests() < 1) {
                throw ApiException.invalid();
            }

            r.setNumberOfGuests(d.numberOfGuests());
        }

        // Update total amount
        if (d.totalAmount() != null) {

            if (d.totalAmount().signum() < 0) {
                throw ApiException.invalid();
            }

            r.setTotalAmount(d.totalAmount());
        }

        // Update reservation status
        if (d.status() != null) {
            r.setStatus(d.status());
        }

        // Update room ID
        if (d.roomId() != null) {

            if (d.roomId() <= 0) {
                throw ApiException.invalid();
            }

            // Check room existence and status
            RoomDTO room = getRoom(d.roomId());

            if (room.status() != RoomStatus.AVAILABLE) {
                throw ApiException.roomNotAvailable();
            }

            r.setRoomId(d.roomId());
        }

        return dto(repo.save(r));
    }

    // Get reservation details with room information
    @Override
    @Transactional(readOnly = true)
    public ReservationDetailDTO detail(Long id) {

        Reservation r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        RoomDTO room = getRoom(r.getRoomId());

        return detailDto(r, room);
    }

    // Cancel a reservation
    @Override
    @Transactional
    public void cancel(Long id) {

        Reservation r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        r.setStatus(ReservationStatus.CANCELLED);

        repo.save(r);
    }

    // Get paginated reservations with optional guestName and status filters
    @Override
    @Transactional(readOnly = true)
    public PageDTO<ReservationDetailDTO> list(
            int page, int size, String guestName, String status) {

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.invalid();
        }

        ReservationStatus reservationStatus = parseStatus(status);

        Pageable p = PageRequest.of(page, size, Sort.by("reservationId"));

        Specification<Reservation> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (guestName != null && !guestName.isBlank()) {
                ps.add(cb.like(
                        cb.lower(root.get("guestName")),
                        "%" + guestName.trim().toLowerCase() + "%"));
            }
            if (reservationStatus != null) {
                ps.add(cb.equal(root.get("status"), reservationStatus));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Reservation> result = repo.findAll(spec, p);

        // Cache room information to avoid repeated Feign calls
        Map<Long, RoomDTO> cache = new HashMap<>();

        return PageDTO.of(
                result.map(r -> detailDto(
                        r,
                        cache.computeIfAbsent(r.getRoomId(), this::getRoom)
                ))
        );
    }

    // Convert String to ReservationStatus Enum
    private ReservationStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ReservationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    // Get room information from Room Service using Feign
    private RoomDTO getRoom(Long id) {

        try {
            ApiResponseDTO<RoomDTO> response = roomClient.getRoom(id);

            if (response == null || response.data() == null) {
                throw ApiException.missing();
            }

            return response.data();

        } catch (FeignException.NotFound e) {

            throw ApiException.missing();

        } catch (FeignException e) {

            throw new IllegalStateException("Room Service is unavailable", e);
        }
    }

    // total = nights * pricePerNight
    private BigDecimal calcTotal(LocalDate in, LocalDate out, RoomDTO room) {
        long nights = ChronoUnit.DAYS.between(in, out);
        return room.pricePerNight().multiply(BigDecimal.valueOf(nights));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    // Convert Reservation entity to ReservationDTO
    private ReservationDTO dto(Reservation r) {

        return new ReservationDTO(
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

    // Convert Reservation entity and RoomDTO to ReservationDetailDTO
    private ReservationDetailDTO detailDto(Reservation r, RoomDTO room) {

        return new ReservationDetailDTO(
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
