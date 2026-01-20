package com.ab.notification.batch.reader;

import com.ab.notification.annotation.Log;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.repository.ErrorBatchRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@StepScope
public class FailedEmailsReader implements ItemReader<ErrorBatchEntity> {
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    private volatile List<ErrorBatchEntity> errorBatchEntities;

    private ErrorBatchRepository errorBatchRepository;

    public FailedEmailsReader(ErrorBatchRepository errorBatchRepository) {
        this.errorBatchRepository = errorBatchRepository;
    }

    @Override
    @Log
    public ErrorBatchEntity read() {
//      In case multiple threads access read() simultaneously then DB is not called twice
        if (errorBatchEntities == null) {
            synchronized (this) {
                if (errorBatchEntities == null) {
                    errorBatchEntities = errorBatchRepository.findIfSuccessIsFalse();
                }
            }
        }

//      Here we get current index -> So 2 threads never get same index as current Index is Atomic
        int index = currentIndex.getAndIncrement();
        if (index < errorBatchEntities.size()) {
            return errorBatchEntities.get(index);
        } else {
            currentIndex.set(0); // Reset for potential future reads
            return null;
        }
    }
}
