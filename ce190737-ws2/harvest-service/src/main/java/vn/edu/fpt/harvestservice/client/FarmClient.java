package vn.edu.fpt.harvestservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.fpt.harvestservice.dto.FarmApiResponseDTO;

@FeignClient(name = "farm-service", url = "${farm-service.url:http://localhost:8081}")
public interface FarmClient {

    @GetMapping("/api/farms/{id}")
    FarmApiResponseDTO getFarmById(@PathVariable("id") Long id);
}
