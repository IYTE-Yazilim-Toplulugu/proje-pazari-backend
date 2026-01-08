package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserPrincipalTest {

    @Test
    void testUserPrincipalCreation() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";

        // When
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, role);

        // Then
        assertEquals(userId, userPrincipal.getUserId());
        assertEquals(email, userPrincipal.getEmail());
        assertEquals(role, userPrincipal.getRole());
        assertEquals(email, userPrincipal.getUsername());
    }

    @Test
    void testUserAuthorities() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, role);

        // When
        var authorities = userPrincipal.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_USER", authority.getAuthority());
    }

    @Test
    void testAdminAuthorities() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "admin@example.com";
        String role = "ADMIN";
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, role);

        // When
        var authorities = userPrincipal.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority());
    }

    @Test
    void testModeratorAuthorities() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "mod@example.com";
        String role = "MODERATOR";
        UserPrincipal userPrincipal = new UserPrincipal(userId, email, role);

        // When
        var authorities = userPrincipal.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_MODERATOR", authority.getAuthority());
    }

    @Test
    void testAccountStatus() {
        // Given
        UserPrincipal userPrincipal =
                new UserPrincipal("01HQZX9K2M3N4P5Q6R7S8T9V0W", "test@example.com", "USER");

        // Then
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void testPasswordIsNull() {
        // Given
        UserPrincipal userPrincipal =
                new UserPrincipal("01HQZX9K2M3N4P5Q6R7S8T9V0W", "test@example.com", "USER");

        // Then
        assertNull(userPrincipal.getPassword());
    }
}
