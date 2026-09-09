package com.iyte_yazilim.proje_pazari.domain.events;

import com.iyte_yazilim.proje_pazari.domain.models.TeamMemberInfo;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpdatedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        List<TeamMemberInfo> teamMembers,
        LocalDateTime occurredOn)
        implements IDomainEvent {}
