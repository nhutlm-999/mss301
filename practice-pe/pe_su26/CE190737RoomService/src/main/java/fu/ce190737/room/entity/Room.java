package fu.ce190737.room.entity;

import jakarta.persistence.*;
import lombok.*;
import fu.ce190737.room.dto.RoomStatus;
import fu.ce190737.room.dto.RoomType;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long roomId;
    String roomNumber;
    @Enumerated(EnumType.STRING)
    RoomType roomType;
    BigDecimal pricePerNight;
    Integer capacity;
    Integer floor;
    @Enumerated(EnumType.STRING)
    RoomStatus status;
}
