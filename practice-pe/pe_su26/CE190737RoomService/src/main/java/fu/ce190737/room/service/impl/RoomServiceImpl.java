package fu.ce190737.room.service.impl;

import fu.ce190737.room.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import fu.ce190737.room.common.ApiException;
import fu.ce190737.room.common.Check;
import fu.ce190737.room.dto.PageDTO;
import fu.ce190737.room.dto.RoomDto;
import fu.ce190737.room.dto.RoomStatus;
import fu.ce190737.room.dto.RoomType;
import fu.ce190737.room.entity.Room;
import fu.ce190737.room.service.RoomService;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository repo;


    RoomDto dto(Room room) {
        return new RoomDto(room.getRoomId(), room.getRoomNumber(), room.getRoomType(), room.getPricePerNight(), room.getCapacity(), room.getFloor(), room.getStatus());
    }

    private Room save(Room r, RoomDto d, boolean isCreate) {
        if (isCreate || d.roomNumber() != null) {
            String num = Check.str(d.roomNumber(), 20);
            if (isCreate ? repo.existsByRoomNumber(num) : repo.existsByRoomNumberAndRoomIdNot(num, d.roomId())) {
                throw ApiException.duplicate();
            }
            r.setRoomNumber(d.roomNumber());
        }
        if (isCreate || d.roomType() != null) {
            r.setRoomType(Check.notNull(d.roomType()));
        }
        if (isCreate || d.pricePerNight() != null) {
            r.setPricePerNight(Check.decimalGtZero(d.pricePerNight()));
        }
        if (isCreate || d.capacity() != null) {
            r.setCapacity(Check.intMin(d.capacity(), 1));
        }
        if (isCreate || d.floor() != null) {
            r.setFloor(Check.intMin(d.floor(), 1));
        }
        if (d.status() != null) {
            r.setStatus(d.status());
        } else if (isCreate) {
            r.setStatus(RoomStatus.AVAILABLE);
        }
        return repo.save(r);
    }

    @Override
    public RoomDto getById(long roomId) {
        return dto(repo.findById(roomId).orElseThrow(ApiException::missing));
    }

    @Override
    public PageDTO<RoomDto> getAll(int page, int size, String roomType, String status, String roomNumber) {
        Check.page(page, size);
        String roomNumberS = (roomNumber != null && !roomNumber.trim().isEmpty()) ? roomNumber.trim() : null;
        RoomType typeEnum = Check.enumParam(RoomType.class, roomType);
        RoomStatus statusEnum = Check.enumParam(RoomStatus.class, status);
        Page<Room> roomPage = repo.search(PageRequest.of(page, size), typeEnum, statusEnum, roomNumberS);
        return PageDTO.of(roomPage.map(this::dto));
    }

    @Override
    public RoomDto create(RoomDto r) {
//        if (repo.existsById(r.roomId())) {
//            throw ApiException.duplicate();
//        }
//        return dto(repo.save(
//                Room.builder()
//                        .roomNumber(Check.str(r.roomNumber(), 1, 100))
//                        .roomType(Check.notNull(r.roomType()))
//                        .pricePerNight(Check.decimalGtZero(r.pricePerNight()))
//                        .capacity(Check.intMin(r.capacity(), 1))
//                        .floor(Check.intMin(r.floor(), 1))
//                        .status(r.status() == null ? RoomStatus.AVAILABLE : r.status())
//                        .build()));
        return dto(save(new Room(), r, true));
    }

    @Override
    public RoomDto update(long roomId, RoomDto roomDto) {
        Room room = repo.findById(roomId).orElseThrow(ApiException::missing);

//        if (roomDto.roomNumber() != null) {
//            String num = Check.str(roomDto.roomNumber(), 20);
//            if (repo.existsByRoomNumberAndRoomIdNot(num, roomId)) {
//                throw ApiException.duplicate();
//            }
//            room.setRoomNumber(roomDto.roomNumber());
//        }
//        if (roomDto.roomType() != null)
//            room.setRoomType(roomDto.roomType());
//        if (roomDto.pricePerNight() != null)
//            room.setPricePerNight(Check.decimalGtZero(roomDto.pricePerNight()));
//        if (roomDto.capacity() != null)
//            room.setCapacity(Check.intMin(roomDto.capacity(), 1));
//        if (roomDto.floor() != null)
//            room.setFloor(Check.intMin(roomDto.floor(), 1));
//        if (roomDto.status() != null)
//            room.setStatus(roomDto.status());
//        Room saved = repo.save(room);
//        return dto(saved);

        return dto(save(room, roomDto, false));
    }

    @Override
    public void delete(long roomId) {
        Room room = repo.findById(roomId).orElseThrow(ApiException::missing);
        room.setStatus(RoomStatus.MAINTENANCE);
        repo.save(room);
    }
}
