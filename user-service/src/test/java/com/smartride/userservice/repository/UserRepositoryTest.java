package com.smartride.userservice.repository;

import com.smartride.userservice.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
//loads jpa component,uses h2 in memory Db by default
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    @BeforeEach
    //before each test we ensures fresh data
    void setUp() {
        userRepository.deleteAll();

        // Build a test user using Lombok's @Builder
        testUser = UserEntity.builder()
                .userEmail("test@example.com")
                .userName("Test User")
                .userPassword("password123")
                .build();

        userRepository.save(testUser);
    }
    @Test
    @DisplayName("Find user by email - user exists")
    void testFindByUserEmail_WhenUserExists() {
        Optional<UserEntity> foundUser = userRepository.findByUserEmail("test@example.com");

        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals("Test User", foundUser.get().getUserName(), "User name should match");
    }

    @Test
    @DisplayName("Find user by email - user does not exist")
    void testFindByUserEmail_UserDoesNotExist() {
        Optional<UserEntity> foundUser = userRepository.findByUserEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent(), "User should not be found");
    }

    @Test
    @DisplayName("Check if user exists by email - user exists")
    void testExistsByUserEmail_UserExists() {
        assertTrue(userRepository.existsByUserEmail("test@example.com"), "User should exist");
    }

    @Test
    @DisplayName("Check if user exists by email - user does not exist")
    void testExistsByUserEmail_UserDoesNotExist() {
        assertFalse(userRepository.existsByUserEmail("nonexistent@example.com"), "User should not exist");
    }
}