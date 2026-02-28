package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService service;

    @Test
    @DisplayName("Should load user details when user exists and is active")
    void shouldLoadUserDetails_whenUserExistsAndActive() {
        // Given
        String email = "test@std.iyte.edu.tr";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-123");
        userEntity.setEmail(email);
        userEntity.setPassword("encoded-password");
        userEntity.setIsActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // When
        UserDetails userDetails = service.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrow_whenUserNotFound() {
        // Given
        String email = "nonexistent@std.iyte.edu.tr";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class, () -> service.loadUserByUsername(email));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should throw when user account is deactivated")
    void shouldThrow_whenAccountIsDeactivated() {
        // Given
        String email = "deactivated@std.iyte.edu.tr";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-456");
        userEntity.setEmail(email);
        userEntity.setPassword("encoded-password");
        userEntity.setIsActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // When & Then
        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class, () -> service.loadUserByUsername(email));
        assertTrue(exception.getMessage().contains("deactivated"));
    }

    @Test
    @DisplayName("Should throw when isActive is null")
    void shouldThrow_whenIsActiveIsNull() {
        // Given
        String email = "nullactive@std.iyte.edu.tr";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-789");
        userEntity.setEmail(email);
        userEntity.setPassword("encoded-password");
        userEntity.setIsActive(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(email));
    }
}
