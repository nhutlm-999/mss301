package fu.ce190737.enrollment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Ce190737EnrollmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ce190737EnrollmentServiceApplication.class, args);
    }

}
