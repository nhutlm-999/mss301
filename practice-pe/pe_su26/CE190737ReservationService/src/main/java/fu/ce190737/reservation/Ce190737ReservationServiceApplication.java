package fu.ce190737.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Ce190737ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ce190737ReservationServiceApplication.class, args);
    }

}
