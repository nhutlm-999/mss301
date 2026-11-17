package fu.ce190737.room.service;

import fu.ce190737.room.dto.PageDTO;
import fu.ce190737.room.dto.RoomDto;

public interface RoomService {
    RoomDto getById(long roomId);

    PageDTO<RoomDto> getAll(int page, int size, String roomType, String status, String roomNumber);

    RoomDto create(RoomDto roomDto);

    RoomDto update(long roomId, RoomDto roomDto);

    void delete(long roomId);
}
