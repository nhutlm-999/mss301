package fu.ce190737.room.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import fu.ce190737.room.dto.RoomStatus;
import fu.ce190737.room.dto.RoomType;
import fu.ce190737.room.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomId(Long roomId);

    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);

    @Query("select r from Room r where" +
            "(:roomNumber is null or LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) and" +
            "(:roomType is null or r.roomType = :roomType) and" +
            "(:status is null or r.status = :status)")
    Page<Room> search(Pageable pageable, @Param("roomType") RoomType roomType, @Param("status") RoomStatus status, @Param("roomNumber") String roomNumber);

    boolean existsByRoomNumber(String roomNumber);
}
