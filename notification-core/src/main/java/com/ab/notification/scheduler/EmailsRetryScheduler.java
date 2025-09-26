package com.ab.notification.scheduler;

import com.ab.notification.annotation.Log;
import com.ab.notification.constants.NotificationContants;
import com.ab.notification.helper.EmailHelper;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.repository.ErrorBatchRepository;
import com.ab.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class will fetch error details and retry email sending periodically
 * //TODO 5) Any Batch processing performed here if yes should be replaced with Spring Batch if advantageous.
 */
@Component
public class EmailsRetryScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailsRetryScheduler.class);

    private final ErrorBatchRepository errorBatchRepository;

    private final EmailHelper emailHelper;

    private final EmailService emailService;

    @Autowired
    public EmailsRetryScheduler(ErrorBatchRepository batchRepository, EmailService service, EmailHelper helper) {
        errorBatchRepository = batchRepository;
        emailHelper = helper;
        emailService = service;

    }

    /**
     * Runs in every 8 hours
     */
//    @Scheduled(cron = "0 24 10 * * 3")
    @Scheduled(fixedDelayString  = "${error.retry.scheduler.delay}")
    @Log
    public void retryFailedEmails() {
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
                errorBatchRepository.saveAll(updatedErrorBatchEntityList);
            }
            sendMailsToAdmins(idsForRetryCountGreaterThanTwo, idsForFailedBatches);
        }
    }

    private void sendMailsToAdmins(StringBuilder idsForRetryCountGreaterThanTwo, StringBuilder idsForFailedBatches) {
        if (!idsForRetryCountGreaterThanTwo.isEmpty()) {
            Map<String, String> mailMapForAdminMail = emailHelper.prepareMailMap(NotificationContants.MAIL_SUBJECT_FOR_RETRYING_MORE_THAN_TWICE_SENDING_TO_ADMIN, String.format(NotificationContants.MAIL_BODY_FOR_RETRYING_MORE_THAN_TWICE_SENDING_TO_ADMIN, idsForRetryCountGreaterThanTwo));
            emailHelper.sendMailToAdmin(mailMapForAdminMail, true);
        }
        if (!idsForFailedBatches.isEmpty()) {
            Map<String, String> mailMapForFailedBatchIdsAdminMail = emailHelper.prepareMailMap(NotificationContants.MAIL_SUBJECT_FOR_RETRYING_FAILURE_ALERT_SENDING_TO_ADMIN, String.format(NotificationContants.MAIL_BODY_FOR_RETRYING_FAILURE_ALERT_SENDING_TO_ADMIN, idsForFailedBatches));
            emailHelper.sendMailToAdmin(mailMapForFailedBatchIdsAdminMail, true);
        }
    }

}
