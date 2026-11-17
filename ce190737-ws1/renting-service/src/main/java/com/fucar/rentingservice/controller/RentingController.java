package com.fucar.rentingservice.controller;

import com.fucar.rentingservice.dto.RentingRequestDTO;
import com.fucar.rentingservice.dto.RentingResponseDTO;
import com.fucar.rentingservice.service.RentingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rentings")
public class RentingController {
    private RentingService rentingService;

    RentingController(RentingService rentingService) {
        this.rentingService = rentingService;
    }

    @PostMapping
    public ResponseEntity<RentingResponseDTO> createRenting(@RequestBody RentingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentingService.createRenting(dto));
    }

    @GetMapping
    public ResponseEntity<List<RentingResponseDTO>> getAllRentings() {
        return ResponseEntity.ok(rentingService.getAllRentings());
    }
}
