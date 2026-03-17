package com.smartride.userservice.repository;

import com.smartride.userservice.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DriverProfileRepository extends JpaRepository<UserEntity,Long> {
}
