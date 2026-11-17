package vn.edu.fpt.harvestservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.harvestservice.dto.ApiResponseDTO;
import vn.edu.fpt.harvestservice.dto.HarvestDTO;
import vn.edu.fpt.harvestservice.dto.HarvestSummaryDTO;
import vn.edu.fpt.harvestservice.dto.PageDTO;
import vn.edu.fpt.harvestservice.service.IHarvestService;
import vn.edu.fpt.harvestservice.util.DateTimeUtils;

@RestController
@RequestMapping("/api/harvests")
@RequiredArgsConstructor
public class HarvestController {

    private final IHarvestService harvestService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO> createHarvest(@RequestBody(required = false) HarvestDTO harvestDTO) {
        HarvestDTO created = harvestService.createHarvest(harvestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO(1, created, DateTimeUtils.nowIsoUtc()));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllHarvests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageDTO pageDTO = harvestService.getAllHarvests(page, size);
        return ResponseEntity.ok(new ApiResponseDTO(1, pageDTO, DateTimeUtils.nowIsoUtc()));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDTO> getHarvestSummary(
            @RequestParam(value = "farmId", required = false) Long farmId
    ) {
        HarvestSummaryDTO summaryDTO = harvestService.getHarvestSummary(farmId);
        return ResponseEntity.ok(new ApiResponseDTO(1, summaryDTO, DateTimeUtils.nowIsoUtc()));
    }
}
