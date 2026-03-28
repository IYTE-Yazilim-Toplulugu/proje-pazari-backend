package com.iyte_yazilim.proje_pazari.application.service;

import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Executor that periodically checks for due scheduled emails and sends them. Runs every 60 seconds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledEmailExecutor {

    private final ScheduledEmailRepository scheduledEmailRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processScheduledEmails() {
        List<ScheduledEmailEntity> dueEmails =
                scheduledEmailRepository.findByStatusAndScheduledAtBefore(
                        "PENDING", LocalDateTime.now());

        for (ScheduledEmailEntity email : dueEmails) {
            try {
                sendBroadcast(email);
                email.setStatus("SENT");
                scheduledEmailRepository.save(email);
                log.info("Scheduled email sent: {} (ID: {})", email.getSubject(), email.getId());
            } catch (Exception e) {
                log.error(
                        "Failed to send scheduled email: {} (ID: {})",
                        email.getSubject(),
                        email.getId(),
                        e);
            }
        }
    }

    private void sendBroadcast(ScheduledEmailEntity email) {
        List<UserEntity> users;
        if (email.getTargetRole() != null && !"ALL".equalsIgnoreCase(email.getTargetRole())) {
            try {
                RoleType role = RoleType.valueOf(email.getTargetRole().toUpperCase());
                users =
                        userRepository
                                .findByRole(
                                        role, org.springframework.data.domain.Pageable.unpaged())
                                .getContent();
            } catch (IllegalArgumentException e) {
                log.warn("Invalid target role for scheduled email: {}", email.getTargetRole());
                users = userRepository.findAll();
            }
        } else {
            users = userRepository.findAll();
        }

        for (UserEntity user : users) {
            if (user.getIsActive() != null && user.getIsActive()) {
                try {
                    emailService.sendEmailAsync(
                            new EmailDto(user.getEmail(), email.getSubject(), email.getBody()));
                } catch (Exception e) {
                    log.error("Failed to send email to: {}", user.getEmail(), e);
                }
            }
        }
    }
}
