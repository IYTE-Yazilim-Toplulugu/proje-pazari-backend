package com.iyte_yazilim.proje_pazari.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ScheduledEmailExecutorTest {

    @Mock private ScheduledEmailRepository scheduledEmailRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks private ScheduledEmailExecutor executor;

    private ScheduledEmailEntity pendingEmail(String targetRole) {
        ScheduledEmailEntity email = new ScheduledEmailEntity();
        email.setId("email-1");
        email.setSubject("Test Subject");
        email.setBody("Test Body");
        email.setTargetRole(targetRole);
        email.setScheduledAt(LocalDateTime.now().minusMinutes(1));
        email.setStatus("PENDING");
        return email;
    }

    private UserEntity activeUser(String emailAddress) {
        UserEntity user = new UserEntity();
        user.setEmail(emailAddress);
        user.setIsActive(true);
        return user;
    }

    private UserEntity inactiveUser(String emailAddress) {
        UserEntity user = new UserEntity();
        user.setEmail(emailAddress);
        user.setIsActive(false);
        return user;
    }

    @Test
    @DisplayName("Should do nothing when no pending emails are due")
    void processScheduledEmails_noPendingEmails_doesNothing() {
        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of());

        executor.processScheduledEmails();

        verifyNoInteractions(emailService);
        verify(scheduledEmailRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should send to all users when targetRole is null")
    void processScheduledEmails_nullTargetRole_sendsToAllUsers() {
        ScheduledEmailEntity email = pendingEmail(null);
        UserEntity user = activeUser("user@test.com");

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of(user));

        executor.processScheduledEmails();

        verify(userRepository).findAll();
        verify(userRepository, never()).findByRole(any(), any());
        verify(emailService).sendEmailAsync(any(EmailDto.class));
    }

    @Test
    @DisplayName("Should send to all users when targetRole is ALL (case-insensitive)")
    void processScheduledEmails_targetRoleAll_sendsToAllUsers() {
        ScheduledEmailEntity email = pendingEmail("all");
        UserEntity user = activeUser("user@test.com");

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of(user));

        executor.processScheduledEmails();

        verify(userRepository).findAll();
        verify(userRepository, never()).findByRole(any(), any());
    }

    @Test
    @DisplayName("Should filter users by role when a valid role is specified")
    void processScheduledEmails_targetRoleStudent_queriesByRole() {
        ScheduledEmailEntity email = pendingEmail("STUDENT");
        UserEntity user = activeUser("student@test.com");
        Page<UserEntity> page = new PageImpl<>(List.of(user));

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findByRole(eq(RoleType.STUDENT), any(Pageable.class))).thenReturn(page);

        executor.processScheduledEmails();

        verify(userRepository).findByRole(eq(RoleType.STUDENT), any(Pageable.class));
        verify(userRepository, never()).findAll();
        verify(emailService).sendEmailAsync(any(EmailDto.class));
    }

    @Test
    @DisplayName("Should fall back to findAll when targetRole is an invalid enum value")
    void processScheduledEmails_invalidTargetRole_fallsBackToFindAll() {
        ScheduledEmailEntity email = pendingEmail("INVALID_ROLE");

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of());

        executor.processScheduledEmails();

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should skip inactive users")
    void processScheduledEmails_inactiveUser_skipsEmail() {
        ScheduledEmailEntity email = pendingEmail(null);

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of(inactiveUser("inactive@test.com")));

        executor.processScheduledEmails();

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should skip users with null isActive flag")
    void processScheduledEmails_userWithNullIsActive_skipsEmail() {
        ScheduledEmailEntity email = pendingEmail(null);
        UserEntity user = new UserEntity();
        user.setEmail("user@test.com");
        user.setIsActive(null);

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of(user));

        executor.processScheduledEmails();

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should set status to SENT and save after successful processing")
    void processScheduledEmails_success_setsStatusSentAndSaves() {
        ScheduledEmailEntity email = pendingEmail(null);

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of());

        executor.processScheduledEmails();

        assertEquals("SENT", email.getStatus());
        verify(scheduledEmailRepository).save(email);
    }

    @Test
    @DisplayName("Should swallow per-user exceptions and continue processing remaining users")
    void processScheduledEmails_sendThrowsForOneUser_continuesWithOthers() {
        ScheduledEmailEntity email = pendingEmail(null);
        UserEntity user1 = activeUser("user1@test.com");
        UserEntity user2 = activeUser("user2@test.com");

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        doThrow(new RuntimeException("send failed"))
                .when(emailService)
                .sendEmailAsync(any(EmailDto.class));

        // Should not throw — exceptions are swallowed per-user
        assertDoesNotThrow(() -> executor.processScheduledEmails());

        // Both users were still attempted
        verify(emailService, times(2)).sendEmailAsync(any(EmailDto.class));
    }

    @Test
    @DisplayName("Should process all pending emails independently")
    void processScheduledEmails_multipleEmails_allProcessed() {
        ScheduledEmailEntity email1 = pendingEmail(null);
        email1.setId("e1");
        ScheduledEmailEntity email2 = pendingEmail(null);
        email2.setId("e2");

        when(scheduledEmailRepository.findByStatusAndScheduledAtBefore(eq("PENDING"), any()))
                .thenReturn(List.of(email1, email2));
        when(userRepository.findAll()).thenReturn(List.of());

        executor.processScheduledEmails();

        verify(scheduledEmailRepository).save(email1);
        verify(scheduledEmailRepository).save(email2);
        assertEquals("SENT", email1.getStatus());
        assertEquals("SENT", email2.getStatus());
    }
}
