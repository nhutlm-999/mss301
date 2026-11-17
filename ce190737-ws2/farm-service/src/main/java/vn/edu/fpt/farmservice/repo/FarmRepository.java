package vn.edu.fpt.farmservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.farmservice.entity.Farm;

import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    boolean existsByFarmCode(String farmCode);

    boolean existsByFarmCodeAndFarmIdNot(String farmCode, Long farmId);

    Optional<Farm> findByFarmCode(String farmCode);
}
