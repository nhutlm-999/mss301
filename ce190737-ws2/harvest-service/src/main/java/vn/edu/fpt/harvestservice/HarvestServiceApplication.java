package vn.edu.fpt.harvestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HarvestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HarvestServiceApplication.class, args);
    }

}
