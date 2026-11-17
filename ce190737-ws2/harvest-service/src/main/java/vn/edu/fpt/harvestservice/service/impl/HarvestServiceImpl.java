package vn.edu.fpt.harvestservice.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.harvestservice.client.FarmClient;
import vn.edu.fpt.harvestservice.dto.*;
import vn.edu.fpt.harvestservice.entity.Harvest;
import vn.edu.fpt.harvestservice.exception.ApiException;
import vn.edu.fpt.harvestservice.repo.HarvestRepository;
import vn.edu.fpt.harvestservice.service.IHarvestService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HarvestServiceImpl implements IHarvestService {

    private static final Set<String> ALLOWED_CROPS = Set.of("RICE", "CORN", "CASSAVA", "SUGARCANE");

    private final HarvestRepository harvestRepository;
    private final FarmClient farmClient;

    @Override
    public HarvestDTO createHarvest(HarvestDTO dto) {
        validateHarvestInput(dto);

        FarmDTO farm = fetchFarm(dto.farmId());
        if (!"ACTIVE".equalsIgnoreCase(farm.status())) {
            throw ApiException.businessRuleViolation("Farm status is not ACTIVE");
        }

        String cropUpper = dto.crop().trim().toUpperCase();
        Date normalizedDate = normalizeToDate(dto.harvestDate());

        if (harvestRepository.existsByFarmIdAndCropAndHarvestDate(dto.farmId(), cropUpper, normalizedDate)) {
            throw ApiException.duplicate("The farm already has a batch with the same crop and harvestDate");
        }

        Harvest harvest = new Harvest(
                null,
                dto.farmId(),
                cropUpper,
                normalizedDate,
                dto.weightKg()
        );

        Harvest saved = harvestRepository.save(harvest);

        return new HarvestDTO(
                saved.getHarvestId(),
                saved.getFarmId(),
                farm.farmCode(),
                farm.name(),
                saved.getCrop(),
                saved.getHarvestDate(),
                saved.getWeightKg()
        );
    }

    @Override
    public PageDTO getAllHarvests(int page, int size) {
        int actualPage = Math.max(page, 0);
        int actualSize = size < 1 ? 10 : Math.min(size, 100);

        Pageable pageable = PageRequest.of(actualPage, actualSize);
        Page<Harvest> harvestPage = harvestRepository.findAll(pageable);

        Map<Long, FarmDTO> farmCache = new HashMap<>();

        List<HarvestDTO> content = harvestPage.getContent().stream()
                .map(h -> {
                    FarmDTO farm = farmCache.computeIfAbsent(h.getFarmId(), this::fetchFarmSafely);
                    String farmCode = farm != null ? farm.farmCode() : null;
                    String farmName = farm != null ? farm.name() : null;
                    return new HarvestDTO(
                            h.getHarvestId(),
                            h.getFarmId(),
                            farmCode,
                            farmName,
                            h.getCrop(),
                            h.getHarvestDate(),
                            h.getWeightKg()
                    );
                })
                .toList();

        return new PageDTO(
                actualSize,
                harvestPage.getNumber(),
                harvestPage.getTotalPages(),
                harvestPage.getTotalElements(),
                harvestPage.isFirst(),
                harvestPage.isLast(),
                content
        );
    }

    @Override
    public HarvestSummaryDTO getHarvestSummary(Long farmId) {
        if (farmId == null) {
            throw ApiException.invalidData("farmId is required");
        }

        FarmDTO farm = fetchFarm(farmId);
        List<Harvest> batches = harvestRepository.findByFarmId(farmId);

        if (batches.isEmpty()) {
            return new HarvestSummaryDTO(
                    farmId,
                    farm.farmCode(),
                    farm.name(),
                    0.0,
                    0,
                    Collections.emptyList()
            );
        }

        double totalWeight = batches.stream()
                .mapToDouble(Harvest::getWeightKg)
                .sum();
        double roundedTotal = BigDecimal.valueOf(totalWeight).setScale(2, RoundingMode.HALF_UP).doubleValue();

        Map<String, Double> weightByCrop = batches.stream()
                .collect(Collectors.groupingBy(
                        Harvest::getCrop,
                        TreeMap::new,
                        Collectors.summingDouble(Harvest::getWeightKg)
                ));

        List<CropWeightDTO> byCrop = weightByCrop.entrySet().stream()
                .map(entry -> new CropWeightDTO(
                        entry.getKey(),
                        BigDecimal.valueOf(entry.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue()
                ))
                .toList();

        return new HarvestSummaryDTO(
                farmId,
                farm.farmCode(),
                farm.name(),
                roundedTotal,
                batches.size(),
                byCrop
        );
    }

    private void validateHarvestInput(HarvestDTO dto) {
        if (dto == null) {
            throw ApiException.invalidData("Request body is required");
        }

        if (dto.farmId() == null) {
            throw ApiException.invalidData("farmId is required");
        }

        if (dto.crop() == null || dto.crop().trim().isEmpty()) {
            throw ApiException.invalidData("crop is required");
        }
        String cropUpper = dto.crop().trim().toUpperCase();
        if (!ALLOWED_CROPS.contains(cropUpper)) {
            throw ApiException.invalidData("crop must be RICE, CORN, CASSAVA, or SUGARCANE");
        }

        if (dto.harvestDate() == null) {
            throw ApiException.invalidData("harvestDate is required");
        }
        LocalDate hDate = dto.harvestDate().toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        LocalDate todayLocal = LocalDate.now();
        LocalDate maxAllowed = todayLocal.isAfter(todayUtc) ? todayLocal : todayUtc;
        if (hDate.isAfter(maxAllowed)) {
            throw ApiException.invalidData("harvestDate cannot be later than today");
        }

        if (dto.weightKg() == null || dto.weightKg() <= 0 || Double.isNaN(dto.weightKg()) || Double.isInfinite(dto.weightKg())) {
            throw ApiException.invalidData("weightKg must be greater than 0");
        }
    }

    private FarmDTO fetchFarm(Long farmId) {
        try {
            FarmApiResponseDTO response = farmClient.getFarmById(farmId);
            if (response == null || response.data() == null || response.status() == 4) {
                throw ApiException.notFound("Farm not found with id: " + farmId);
            }
            return response.data();
        } catch (FeignException.NotFound ex) {
            throw ApiException.notFound("Farm not found with id: " + farmId);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw ApiException.notFound("Farm not found with id: " + farmId);
            }
            throw ex;
        }
    }

    private FarmDTO fetchFarmSafely(Long farmId) {
        try {
            return fetchFarm(farmId);
        } catch (Exception e) {
            return null;
        }
    }

    private Date normalizeToDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        return java.sql.Date.valueOf(localDate);
    }
}
