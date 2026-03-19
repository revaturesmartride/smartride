package com.smartride.userservice.security;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map UserRole enum to Spring Security authority
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name());

        return new User(
                user.getUserEmail(),       // username for Spring Security
                user.getUserPassword(),    // password from your entity
                Collections.singletonList(authority) // list of authorities/roles
        );
    }
}
