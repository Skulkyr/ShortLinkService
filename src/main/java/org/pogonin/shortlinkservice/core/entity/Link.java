package org.pogonin.shortlinkservice.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class Link {
    @Id
    @Column(name = "original_link", unique=true, nullable=false)
    private String originalLink;

    @Column(name = "short_link", unique=true, nullable=false)
    private String shortLink;

    @Column(name = "number_of_uses", nullable=false)
    private Long numberOfUses;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_access_time")
    private LocalDateTime lastAccess;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime creationDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    @PostLoad
    private void postLoad() {
        lastAccess = LocalDateTime.now();
        numberOfUses++;
        if (usageLimit != null)
            usageLimit--;
    }
}
