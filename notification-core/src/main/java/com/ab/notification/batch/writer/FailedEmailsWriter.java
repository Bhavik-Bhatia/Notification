package com.ab.notification.batch.writer;

import com.ab.notification.annotation.Log;
import com.ab.notification.constants.NotificationContants;
import com.ab.notification.helper.EmailHelper;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.repository.ErrorBatchRepository;
import com.ab.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class FailedEmailsWriter implements ItemWriter<ErrorBatchEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedEmailsWriter.class);
    private StringBuilder idsForRetryCountGreaterThanTwo = new StringBuilder();
    private StringBuilder idsForFailedBatches = new StringBuilder();
    private List<ErrorBatchEntity> updatedErrorBatchEntityList = new ArrayList<>();
    private final EmailHelper emailHelper;
    private final EmailService emailService;
    private final ErrorBatchRepository errorBatchRepository;
    private ExecutionContext executionContext;

    public FailedEmailsWriter(EmailHelper emailHelper, EmailService emailService, ErrorBatchRepository errorBatchRepository) {
        this.emailHelper = emailHelper;
        this.emailService = emailService;
        this.errorBatchRepository = errorBatchRepository;
    }


    @Override
    @Log
    public void write(Chunk<? extends ErrorBatchEntity> chunk) throws Exception {
        for (ErrorBatchEntity errorBatchEntity : chunk.getItems()) {
            try {
            if (!errorBatchEntity.isSuccess()) {
                if (errorBatchEntity.getRetryCount() >= 2) {
                    LOGGER.debug("Retry count is equal or more than 2 for ID {} Please check reason", errorBatchEntity.getId());
                    idsForRetryCountGreaterThanTwo.append(errorBatchEntity.getId().toString()).append(",");
                }
                Map<String, String> mailMapForFailedBatchRetry = emailHelper.prepareMailMap(errorBatchEntity.getMailSubject(), errorBatchEntity.getMailBody());
                emailService.sendBatchMails(mailMapForFailedBatchRetry, errorBatchEntity.getErrorBatchDetail().split(","), true);
                errorBatchEntity.setSuccess(true);
            }
            } catch (Exception e) {
                LOGGER.debug("Exception occurred {} while retrying for ID {}", e.getMessage(), errorBatchEntity.getId());
                idsForFailedBatches.append(errorBatchEntity.getId().toString()).append(",");
            }
            errorBatchEntity.setRetryCount(errorBatchEntity.getRetryCount() + 1);
            errorBatchEntity.setLastRetryAt(ZonedDateTime.now());
            updatedErrorBatchEntityList.add(errorBatchEntity);
        }
//      To update status/retry count/time in DB
        errorBatchRepository.saveAll(updatedErrorBatchEntityList);
        sendMailsToAdmins(idsForRetryCountGreaterThanTwo, idsForFailedBatches);


//      For Next Step Reader
/*      ExecutionContext context = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
        context.put("idsForFailedBatches", idsForFailedBatches);
        context.put("idsForRetryCountGreaterThanTwo", idsForRetryCountGreaterThanTwo);
*/
    }

    @Log
    public void sendMailsToAdmins(StringBuilder idsForRetryCountGreaterThanTwo, StringBuilder idsForFailedBatches) {
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
