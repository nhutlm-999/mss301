package vn.edu.fpt.farmservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.farmservice.dto.ApiResponseDTO;
import vn.edu.fpt.farmservice.dto.FarmDTO;
import vn.edu.fpt.farmservice.dto.PageDTO;
import vn.edu.fpt.farmservice.service.IFarmService;
import vn.edu.fpt.farmservice.util.DateTimeUtils;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final IFarmService farmService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO> createFarm(@RequestBody FarmDTO farmDTO) {
        FarmDTO created = farmService.createFarm(farmDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO(1, created, DateTimeUtils.nowIsoUtc()));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllFarms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageDTO pageDTO = farmService.getAllFarms(page, size);
        return ResponseEntity.ok(new ApiResponseDTO(1, pageDTO, DateTimeUtils.nowIsoUtc()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getFarmById(@PathVariable Long id) {
        FarmDTO farmDTO = farmService.getFarmById(id);
        return ResponseEntity.ok(new ApiResponseDTO(1, farmDTO, DateTimeUtils.nowIsoUtc()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> updateFarm(
            @PathVariable Long id,
            @RequestBody FarmDTO farmDTO
    ) {
        FarmDTO updated = farmService.updateFarm(id, farmDTO);
        return ResponseEntity.ok(new ApiResponseDTO(1, updated, DateTimeUtils.nowIsoUtc()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateFarm(@PathVariable Long id) {
        farmService.deactivateFarm(id);
        return ResponseEntity.noContent().build();
    }
}
