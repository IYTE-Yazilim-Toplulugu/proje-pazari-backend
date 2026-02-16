package com.iyte_yazilim.proje_pazari.application.common;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.AuditLogRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** AOP aspect that intercepts @Audited methods and persists audit log entries. */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String userId = getCurrentUserId();

        AuditLogEntity auditLog =
                AuditLogEntity.builder()
                        .action(audited.action())
                        .entityType(audited.entityType())
                        .performedBy(userId)
                        .timestamp(LocalDateTime.now())
                        .build();

        try {
            Object result = joinPoint.proceed();
            auditLog.setStatus("SUCCESS");
            auditLog.setDetails("Method: " + joinPoint.getSignature().getName());
            return result;
        } catch (Exception e) {
            auditLog.setStatus("FAILED");
            auditLog.setDetails(e.getMessage());
            throw e;
        } finally {
            try {
                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                log.error("Failed to save audit log", e);
            }
        }
    }

    private String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.warn("Could not determine current user for audit log", e);
        }
        return "SYSTEM";
    }
}
