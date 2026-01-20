package com.ab.notification.actuator.metrics;

import com.ab.notification.repository.ErrorBatchRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Guard;

/**
 * Counter for send mails accessed by: /actuator/metrics/mail.sent.count

 * Gauge for current count of failed mails accessed by: /actuator/metrics/failed.mails

 * Rest metrics for processing & memory:
 * <p>
 * /actuator/metrics/jvm.memory.used
 * /actuator/metrics/system.cpu.usage
 * /actuator/metrics/process.uptime
 * /actuator/metrics/jvm.threads.live
 * Counter = “How many times has this happened?”
 * <p>
 * Gauge = “What’s the current value right now?”
 *
 */
@Component
public class MailMetrics {

    private Counter mailSenderCounter;

    private final ErrorBatchRepository errorBatchRepository;

    @Autowired
    public MailMetrics(MeterRegistry meterRegistry, ErrorBatchRepository errorBatchRepository) {
        this.errorBatchRepository = errorBatchRepository;
        this.mailSenderCounter = Counter.builder("mail.sent.count").description("Counts how many mails sent").register(meterRegistry);
        Gauge.builder("failed.mails", errorBatchRepository, ErrorBatchRepository::countIfSuccessIsFalse).description("Current value of how many mails failed").register(meterRegistry);
    }

    public void incrementMailSenderCounter() {
        mailSenderCounter.increment();
    }
}
