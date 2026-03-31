package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for scheduled email persistence operations. */
public interface ScheduledEmailRepository extends JpaRepository<ScheduledEmailEntity, String> {

    List<ScheduledEmailEntity> findByStatus(String status);

    List<ScheduledEmailEntity> findByStatusAndScheduledAtBefore(
            String status, LocalDateTime dateTime);
}
