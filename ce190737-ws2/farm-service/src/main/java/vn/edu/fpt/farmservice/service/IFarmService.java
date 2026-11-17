package vn.edu.fpt.farmservice.service;

import vn.edu.fpt.farmservice.dto.FarmDTO;
import vn.edu.fpt.farmservice.dto.PageDTO;

public interface IFarmService {
    FarmDTO createFarm(FarmDTO dto);

    PageDTO getAllFarms(int page, int size);

    FarmDTO getFarmById(Long id);

    FarmDTO updateFarm(Long id, FarmDTO dto);

    void deactivateFarm(Long id);
}
