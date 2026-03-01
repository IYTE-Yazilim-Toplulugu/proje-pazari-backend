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
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigEntity {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        updatedAt = LocalDateTime.now();
    }
}
