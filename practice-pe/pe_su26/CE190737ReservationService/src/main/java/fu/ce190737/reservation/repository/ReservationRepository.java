package fu.ce190737.reservation.repository;

import fu.ce190737.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import fu.ce190737.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByGuestNameContainingAndStatus(String guestName, String status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE " +
            "(:guestName IS NULL OR LOWER(r.guestName) LIKE LOWER(CONCAT('%', :guestName, '%'))) AND " +
            "(:status IS NULL OR r.status = :status)")
    Page<Reservation> search(@Param("guestName") String guestName,
                             @Param("status") ReservationStatus status,
                             Pageable pageable);
}