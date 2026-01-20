package com.ab.notification.scheduler;

import com.ab.notification.annotation.Log;
import com.ab.notification.batch.listener.NewsletterJobListener;
import com.ab.notification.constants.NotificationContants;
import com.ab.notification.helper.EmailHelper;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.repository.ErrorBatchRepository;
import com.ab.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class will fetch error details and retry email sending periodically
 * //TODO 5) Any Batch processing performed here if yes should be replaced with Spring Batch if advantageous.
 */
@Component
public class EmailsRetryScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailsRetryScheduler.class);

    private final EmailHelper emailHelper;

    private final JobLauncher jobLauncher;

    private final Job job;

    @Autowired
    public EmailsRetryScheduler(@Qualifier("notificationRetryJob") Job job, JobLauncher jobLauncher, EmailHelper helper) {
        this.emailHelper = helper;
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    /**
     * Runs in every 8 hours
     */
//    @Scheduled(cron = "0 24 10 * * 3")
    @Scheduled(fixedDelayString = "${error.retry.scheduler.delay}")
    @Log
    public void retryFailedEmails() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        String jobID = UUID.randomUUID() + "retryFailedEmails";
        JobParameters jobParameter = new JobParametersBuilder().
                addJobParameter("jobId", jobID, String.class).
                addJobParameter("timestamp", System.currentTimeMillis(), Long.class).
                addJobParameter("isRetry", true, Boolean.class).
                toJobParameters();
        jobLauncher.run(job, jobParameter);
/*
        List<ErrorBatchEntity> errorBatchEntityList = errorBatchRepository.findIfSuccessIsFalse();
        StringBuilder idsForRetryCountGreaterThanTwo = new StringBuilder();
        StringBuilder idsForFailedBatches = new StringBuilder();
        List<ErrorBatchEntity> updatedErrorBatchEntityList = new ArrayList<>();
        if (!errorBatchEntityList.isEmpty()) {
            for (ErrorBatchEntity errorBatchEntity : errorBatchEntityList) {
                try {
                    if (errorBatchEntity.getRetryCount() >= 2) {
                        LOGGER.debug("Retry count is equal or more than 2 for ID {} Please check reason", errorBatchEntity.getId());
                        idsForRetryCountGreaterThanTwo.append(errorBatchEntity.getId().toString()).append(",");
                    }
                    Map<String, String> mailMapForFailedBatchRetry = emailHelper.prepareMailMap(errorBatchEntity.getMailSubject(), errorBatchEntity.getMailBody());
                    emailService.sendBatchMails(mailMapForFailedBatchRetry, errorBatchEntity.getErrorBatchDetail().split(","), true);
                    errorBatchEntity.setSuccess(true);
                } catch (Exception e) {
                    LOGGER.debug("Exception occurred {} while retrying for ID {}", e.getMessage(), errorBatchEntity.getId());
                    idsForFailedBatches.append(errorBatchEntity.getId().toString()).append(",");
                }
                errorBatchEntity.setRetryCount(errorBatchEntity.getRetryCount() + 1);
                errorBatchEntity.setLastRetryAt(ZonedDateTime.now());
                updatedErrorBatchEntityList.add(errorBatchEntity);
            }
            errorBatchRepository.saveAll(updatedErrorBatchEntityList);
            sendMailsToAdmins(idsForRetryCountGreaterThanTwo, idsForFailedBatches);
        }
*/
    }
}
