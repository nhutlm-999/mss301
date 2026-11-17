package fu.ce190284.room.repository;

import fu.ce190284.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);
}
