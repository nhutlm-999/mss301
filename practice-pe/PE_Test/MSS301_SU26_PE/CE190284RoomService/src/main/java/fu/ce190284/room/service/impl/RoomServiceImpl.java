package fu.ce190284.room.service.impl;

import fu.ce190284.room.common.ApiException;
import fu.ce190284.room.dto.PageDTO;
import fu.ce190284.room.dto.RoomDTO;
import fu.ce190284.room.entity.Room;
import fu.ce190284.room.enums.RoomStatus;
import fu.ce190284.room.enums.RoomType;
import fu.ce190284.room.repository.RoomRepository;
import fu.ce190284.room.service.RoomService;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository repo;

    // Create a new room
    @Override
    @Transactional
    public RoomDTO create(RoomDTO d) {

        // Validate required fields
        if (blank(d.roomNumber())
                || d.roomNumber().length() > 20
                || d.roomType() == null
                || !validPrice(d.pricePerNight())
                || !validCapacity(d.capacity())
                || !validFloor(d.floor())) {

            throw ApiException.invalid();
        }

        // Check duplicate room number
        if (repo.existsByRoomNumber(d.roomNumber())) {
            throw ApiException.duplicate();
        }

        // Create room with default AVAILABLE status
        Room r = Room.builder()
                .roomNumber(d.roomNumber())
                .roomType(d.roomType())
                .pricePerNight(d.pricePerNight())
                .capacity(d.capacity())
                .floor(d.floor())
                .status(d.status() == null
                        ? RoomStatus.AVAILABLE
                        : d.status())
                .build();

        return dto(repo.save(r));
    }

    // Update an existing room
    @Override
    @Transactional
    public RoomDTO update(Long id, RoomDTO d) {

        Room r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        // Update room number
        if (d.roomNumber() != null) {

            if (blank(d.roomNumber())
                    || d.roomNumber().length() > 20) {

                throw ApiException.invalid();
            }

            // Check duplicate room number
            if (repo.existsByRoomNumberAndRoomIdNot(d.roomNumber(), id)) {
                throw ApiException.duplicate();
            }

            r.setRoomNumber(d.roomNumber());
        }

        // Update room type
        if (d.roomType() != null) {
            r.setRoomType(d.roomType());
        }

        // Update price per night
        if (d.pricePerNight() != null) {

            if (!validPrice(d.pricePerNight())) {
                throw ApiException.invalid();
            }

            r.setPricePerNight(d.pricePerNight());
        }

        // Update capacity
        if (d.capacity() != null) {

            if (!validCapacity(d.capacity())) {
                throw ApiException.invalid();
            }

            r.setCapacity(d.capacity());
        }

        // Update floor
        if (d.floor() != null) {

            if (!validFloor(d.floor())) {
                throw ApiException.invalid();
            }

            r.setFloor(d.floor());
        }

        // Update room status
        if (d.status() != null) {
            r.setStatus(d.status());
        }

        return dto(repo.save(r));
    }

    // Get room by ID
    @Override
    @Transactional(readOnly = true)
    public RoomDTO getById(Long id) {

        Room r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        return dto(r);
    }

    // Soft delete: put the room under MAINTENANCE
    @Override
    @Transactional
    public void delete(Long id) {

        Room r = repo.findById(id)
                .orElseThrow(ApiException::missing);

        r.setStatus(RoomStatus.MAINTENANCE);

        repo.save(r);
    }

    // Get paginated rooms with optional roomNumber, roomType and status filters
    @Override
    @Transactional(readOnly = true)
    public PageDTO<RoomDTO> getAll(
            int page, int size, String roomNumber, String roomType, String status) {

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.invalid();
        }

        RoomType type = parseType(roomType);
        RoomStatus roomStatus = parseStatus(status);

        Pageable p = PageRequest.of(page, size, Sort.by("roomId"));

        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (roomNumber != null && !roomNumber.isBlank()) {
                ps.add(cb.like(
                        cb.lower(root.get("roomNumber")),
                        "%" + roomNumber.trim().toLowerCase() + "%"));
            }
            if (type != null) {
                ps.add(cb.equal(root.get("roomType"), type));
            }
            if (roomStatus != null) {
                ps.add(cb.equal(root.get("status"), roomStatus));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Room> result = repo.findAll(spec, p);

        return PageDTO.of(result.map(this::dto));
    }

    // Convert String to RoomType Enum
    private RoomType parseType(String type) {

        if (type == null || type.isBlank()) {
            return null;
        }

        try {
            return RoomType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    // Convert String to RoomStatus Enum
    private RoomStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return RoomStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    private boolean blank(String v) {
        return v == null || v.isBlank();
    }

    // price_per_night > 0
    private boolean validPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    // capacity between 1 and 10
    private boolean validCapacity(Integer capacity) {
        return capacity != null && capacity >= 1 && capacity <= 10;
    }

    // floor >= 1
    private boolean validFloor(Integer floor) {
        return floor != null && floor >= 1;
    }

    // Convert Room entity to RoomDTO
    private RoomDTO dto(Room r) {

        return new RoomDTO(
                r.getRoomId(),
                r.getRoomNumber(),
                r.getRoomType(),
                r.getPricePerNight(),
                r.getCapacity(),
                r.getFloor(),
                r.getStatus()
        );
    }
}
