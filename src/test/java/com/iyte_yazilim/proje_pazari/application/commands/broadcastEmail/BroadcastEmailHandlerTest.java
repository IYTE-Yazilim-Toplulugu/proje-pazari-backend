package com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BroadcastEmailHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks private BroadcastEmailHandler handler;

    private UserEntity activeUser;
    private UserEntity inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = new UserEntity();
        activeUser.setId("user-1");
        activeUser.setEmail("active@example.com");
        activeUser.setIsActive(true);
        activeUser.setRoles(new HashSet<>(Set.of(RoleType.USER)));

        inactiveUser = new UserEntity();
        inactiveUser.setId("user-2");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setIsActive(false);
        inactiveUser.setRoles(new HashSet<>(Set.of(RoleType.USER)));
    }

    @Test
    @DisplayName("Should broadcast email to all active users")
    void shouldBroadcastEmailToAllActiveUsers() {
        when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));

        BroadcastEmailCommand command =
                new BroadcastEmailCommand("Test Subject", "Test Body", "ALL");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("1 recipients"));
        verify(emailService, times(1)).sendEmailAsync(any(EmailDto.class));
    }

    @Test
    @DisplayName("Should broadcast email to users with specific role")
    void shouldBroadcastEmailToSpecificRole() {
        when(userRepository.findByRole(eq(RoleType.USER), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activeUser)));

        BroadcastEmailCommand command = new BroadcastEmailCommand("Subject", "Body", "USER");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("1 recipients"));
    }

    @Test
    @DisplayName("Should return validation error when subject is empty")
    void shouldReturnValidationErrorForEmptySubject() {
        BroadcastEmailCommand command = new BroadcastEmailCommand("", "Body", "ALL");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("subject"));
        verify(emailService, never()).sendEmailAsync(any());
    }

    @Test
    @DisplayName("Should return validation error when body is empty")
    void shouldReturnValidationErrorForEmptyBody() {
        BroadcastEmailCommand command = new BroadcastEmailCommand("Subject", "", "ALL");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("body"));
        verify(emailService, never()).sendEmailAsync(any());
    }

    @Test
    @DisplayName("Should return validation error for invalid role")
    void shouldReturnValidationErrorForInvalidRole() {
        BroadcastEmailCommand command =
                new BroadcastEmailCommand("Subject", "Body", "INVALID_ROLE");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Invalid target role"));
    }
}
