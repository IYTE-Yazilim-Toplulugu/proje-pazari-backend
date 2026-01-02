package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testRoleBasedAuthorization() throws Exception {
        // Given - Register and login user
        RegisterUserCommand registerCommand =
                new RegisterUserCommand(
                        "roletest@example.com", "SecurePass123!", "Role", "Test");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerCommand)))
                .andExpect(status().isCreated());

        LoginUserCommand loginCommand =
                new LoginUserCommand("roletest@example.com", "SecurePass123!");

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginCommand)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.token").exists())
                        .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();

        // When - Extract role from token
        String role = jwtUtil.extractRole(token);

        // Then
        assert role != null;
        assert role.equals("USER");
    }

    @Test
    void testTokenContainsAllClaims() throws Exception {
        // Given
        RegisterUserCommand registerCommand =
                new RegisterUserCommand(
                        "claims@example.com", "SecurePass123!", "Claims", "Test");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerCommand)))
                .andExpect(status().isCreated());

        LoginUserCommand loginCommand = new LoginUserCommand("claims@example.com", "SecurePass123!");

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginCommand)))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();

        // When
        String userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        // Then
        assert userId != null && !userId.isEmpty();
        assert email != null && email.equals("claims@example.com");
        assert role != null && role.equals("USER");
    }

    @Test
    void testAuthenticatedEndpointWithToken() throws Exception {
        // Given
        RegisterUserCommand registerCommand =
                new RegisterUserCommand(
                        "authtest@example.com", "SecurePass123!", "Auth", "Test");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerCommand)))
                .andExpect(status().isCreated());

        LoginUserCommand loginCommand =
                new LoginUserCommand("authtest@example.com", "SecurePass123!");

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginCommand)))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("token").asText();

        // When/Then - Access protected endpoint with token
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testAuthenticatedEndpointWithoutToken() throws Exception {
        // When/Then - Access protected endpoint without token
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isForbidden());
    }
}