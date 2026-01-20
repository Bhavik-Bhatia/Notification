package com.ab.notification.repository;

import com.ab.notification.model.ErrorBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorBatchRepository extends JpaRepository<ErrorBatchEntity, Long> {

    @Query(nativeQuery = true, value = "select * from notification_service_error_batch_ms_tbl where is_success=false")
    List<ErrorBatchEntity> findIfSuccessIsFalse();

    @Query(nativeQuery = true, value = "select count(*) from notification_service_error_batch_ms_tbl where is_success=false")
    int countIfSuccessIsFalse();

}
