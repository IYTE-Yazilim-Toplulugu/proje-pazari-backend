package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flagged_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggedContentEntity {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "content_type", nullable = false)
    private String contentType; // USER, PROJECT, APPLICATION

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Column(nullable = false)
    private String reason; // SPAM, INAPPROPRIATE, OFFENSIVE

    @Column(name = "reported_by", nullable = false)
    private String reportedBy;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REMOVED

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        if (status == null) {
            status = "PENDING";
        }
        createdAt = LocalDateTime.now();
    }
}
