package com.fucar.rentingservice.repository;

import com.fucar.rentingservice.entity.Renting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentingRepository extends JpaRepository<Renting, Integer> {
}
