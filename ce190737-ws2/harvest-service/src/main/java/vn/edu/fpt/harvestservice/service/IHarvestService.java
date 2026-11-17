package vn.edu.fpt.harvestservice.service;

import vn.edu.fpt.harvestservice.dto.HarvestDTO;
import vn.edu.fpt.harvestservice.dto.HarvestSummaryDTO;
import vn.edu.fpt.harvestservice.dto.PageDTO;

public interface IHarvestService {

    HarvestDTO createHarvest(HarvestDTO harvestDTO);

    PageDTO getAllHarvests(int page, int size);

    HarvestSummaryDTO getHarvestSummary(Long farmId);
}
