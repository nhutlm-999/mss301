package vn.edu.fpt.harvestservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.harvestservice.entity.Harvest;

import java.util.Date;
import java.util.List;

@Repository
public interface HarvestRepository extends JpaRepository<Harvest, Long> {

    boolean existsByFarmIdAndCropAndHarvestDate(Long farmId, String crop, Date harvestDate);

    List<Harvest> findByFarmId(Long farmId);
}
