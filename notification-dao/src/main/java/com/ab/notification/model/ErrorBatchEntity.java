package com.ab.notification.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.ZonedDateTime;

@Entity
@Table(name = "notification_service_error_batch_ms_tbl")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ErrorBatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "error_batch_detail")
    private String errorBatchDetail;

    @Column(name = "mail_subject")
    private String mailSubject;

    @Column(name = "mail_body")
    private String mailBody;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "is_success")
    private boolean isSuccess;

    @Column(name = "last_retry_at")
    private ZonedDateTime lastRetryAt;

    @Column(name = "created_date", updatable = false)
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime createdDate;

    @Column(name = "updated_date")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private ZonedDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        createdDate = ZonedDateTime.now();
        updatedDate = createdDate;
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = ZonedDateTime.now();
    }
}
