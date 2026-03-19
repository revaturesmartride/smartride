package com.smartride.userservice.config;

import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if(userRepository.findByUserEmail("admin@smartride.com").isEmpty()){

            UserEntity admin = new UserEntity();
            admin.setUserName("Admin");
            admin.setUserEmail("admin@smartride.com");
            admin.setUserPassword(passwordEncoder.encode("admin123"));
            admin.setUserRole(UserRole.ADMIN);

            userRepository.save(admin);
        }
    }
}
