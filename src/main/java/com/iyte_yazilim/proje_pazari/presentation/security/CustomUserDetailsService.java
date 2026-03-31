package com.iyte_yazilim.proje_pazari.presentation.security;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "User not found with email: " + email));

        // Check if user account is active
        if (userEntity.getIsActive() == null || !userEntity.getIsActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        return new User(userEntity.getEmail(), userEntity.getPassword(), new ArrayList<>());
    }
}
