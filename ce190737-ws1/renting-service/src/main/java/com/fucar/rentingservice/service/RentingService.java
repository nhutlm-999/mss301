package com.fucar.rentingservice.service;

import com.fucar.rentingservice.dto.RentingRequestDTO;
import com.fucar.rentingservice.dto.RentingResponseDTO;

import java.util.List;

public interface RentingService {
    RentingResponseDTO createRenting(RentingRequestDTO request);
    List<RentingResponseDTO> getAllRentings();
}
