package vn.edu.fpt.farmservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.farmservice.dto.FarmDTO;
import vn.edu.fpt.farmservice.dto.PageDTO;
import vn.edu.fpt.farmservice.entity.Farm;
import vn.edu.fpt.farmservice.exception.ApiException;
import vn.edu.fpt.farmservice.repo.FarmRepository;
import vn.edu.fpt.farmservice.service.IFarmService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements IFarmService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "INACTIVE", "CLOSED");
    private final FarmRepository farmRepository;

    @Override
    public FarmDTO createFarm(FarmDTO dto) {
        String validatedStatus = validateInput(dto, true);

        String farmCode = dto.farmCode().trim();
        if (farmRepository.existsByFarmCode(farmCode)) {
            throw ApiException.duplicate("farmCode '" + farmCode + "' already exists");
        }

        Farm farm = new Farm(
                null,
                farmCode,
                dto.name().trim(),
                dto.location().trim(),
                dto.area(),
                validatedStatus,
                dto.establishedDate()
        );

        Farm saved = farmRepository.save(farm);
        return mapToDTO(saved);
    }

    @Override
    public PageDTO getAllFarms(int page, int size) {
        int actualPage = Math.max(page, 0);
        int actualSize = size;
        if (actualSize < 1) {
            actualSize = 10;
        } else if (actualSize > 100) {
            actualSize = 100;
        }

        Pageable pageable = PageRequest.of(actualPage, actualSize);
        Page<Farm> farmPage = farmRepository.findAll(pageable);

        List<FarmDTO> content = farmPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        return new PageDTO(
                actualSize,
                farmPage.getNumber(),
                farmPage.getTotalPages(),
                farmPage.getTotalElements(),
                farmPage.isFirst(),
                farmPage.isLast(),
                content
        );
    }

    @Override
    public FarmDTO getFarmById(Long id) {
        if (id == null) {
            throw ApiException.notFound("Farm not found");
        }
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Farm not found with id: " + id));
        return mapToDTO(farm);
    }

    @Override
    public FarmDTO updateFarm(Long id, FarmDTO dto) {
        if (id == null) {
            throw ApiException.notFound("Farm not found");
        }
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Farm not found with id: " + id));

        String validatedStatus = validateInput(dto, false);

        String farmCode = dto.farmCode().trim();
        if (farmRepository.existsByFarmCodeAndFarmIdNot(farmCode, id)) {
            throw ApiException.duplicate("farmCode '" + farmCode + "' is already in use by another farm");
        }

        farm.setFarmCode(farmCode);
        farm.setName(dto.name().trim());
        farm.setLocation(dto.location().trim());
        farm.setArea(dto.area());
        farm.setStatus(validatedStatus);
        farm.setEstablishedDate(dto.establishedDate());

        Farm updated = farmRepository.save(farm);
        return mapToDTO(updated);
    }

    @Override
    public void deactivateFarm(Long id) {
        if (id == null) {
            throw ApiException.notFound("Farm not found");
        }
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Farm not found with id: " + id));

        if ("CLOSED".equalsIgnoreCase(farm.getStatus())) {
            throw ApiException.businessRuleViolation("Farm is already CLOSED and cannot be deactivated");
        }

        farm.setStatus("INACTIVE");
        farmRepository.save(farm);
    }

    private String validateInput(FarmDTO dto, boolean isCreate) {
        if (dto == null) {
            throw ApiException.invalidData("Request body is required");
        }

        if (dto.farmCode() == null || dto.farmCode().trim().isEmpty()) {
            throw ApiException.invalidData("farmCode is required");
        }
        String farmCode = dto.farmCode().trim();
        if (farmCode.length() > 20) {
            throw ApiException.invalidData("farmCode must not exceed 20 characters");
        }
        if (!farmCode.matches("^[A-Za-z0-9-]+$")) {
            throw ApiException.invalidData("farmCode must contain only letters, digits, and hyphens");
        }

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw ApiException.invalidData("name is required");
        }
        if (dto.name().trim().length() > 100) {
            throw ApiException.invalidData("name must not exceed 100 characters");
        }

        if (dto.location() == null || dto.location().trim().isEmpty()) {
            throw ApiException.invalidData("location is required");
        }
        if (dto.location().trim().length() > 100) {
            throw ApiException.invalidData("location must not exceed 100 characters");
        }

        if (dto.area() == null || dto.area() <= 0 || Double.isNaN(dto.area()) || Double.isInfinite(dto.area())) {
            throw ApiException.invalidData("area must be greater than 0");
        }

        if (dto.establishedDate() == null) {
            throw ApiException.invalidData("establishedDate is required");
        }
        LocalDate estDate = dto.establishedDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();
        if (estDate.isAfter(today)) {
            throw ApiException.invalidData("establishedDate cannot be later than today");
        }

        String status = dto.status();
        if (isCreate) {
            if (status == null || status.trim().isEmpty()) {
                return "ACTIVE";
            }
        } else {
            if (status == null || status.trim().isEmpty()) {
                throw ApiException.invalidData("status is required");
            }
        }

        String upperStatus = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(upperStatus)) {
            throw ApiException.invalidData("status must be ACTIVE, INACTIVE, or CLOSED");
        }
        return upperStatus;
    }

    private FarmDTO mapToDTO(Farm farm) {
        return new FarmDTO(
                farm.getFarmId(),
                farm.getFarmCode(),
                farm.getName(),
                farm.getLocation(),
                farm.getArea(),
                farm.getStatus(),
                farm.getEstablishedDate()
        );
    }
}
