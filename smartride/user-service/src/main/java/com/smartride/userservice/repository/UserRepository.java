package com.smartride.userservice.repository;

import com.smartride.userservice.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface UserRepository extends JpaRepository<UserEntity,Integer> {

    Optional<UserEntity> findByUserEmail(String userEmail);
    boolean existsByUserEmail(String userEmail);


}
