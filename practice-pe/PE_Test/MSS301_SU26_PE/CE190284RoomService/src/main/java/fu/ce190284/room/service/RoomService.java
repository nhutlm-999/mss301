package fu.ce190284.room.service;

import fu.ce190284.room.dto.*;

public interface RoomService {
    RoomDTO create(RoomDTO d);

    RoomDTO update(Long id, RoomDTO d);

    RoomDTO getById(Long id);

    void delete(Long id);

    PageDTO<RoomDTO> getAll(int page, int size, String roomNumber, String roomType, String status);
}
