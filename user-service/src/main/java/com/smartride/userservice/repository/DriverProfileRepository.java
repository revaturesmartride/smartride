package com.smartride.userservice.repository;

import com.smartride.userservice.model.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

}
