package com.ab.notification.service;

import com.ab.notification.annotation.Log;
import com.ab.notification.constants.NotificationContants;
import com.ab.notification.exception.AppException;
import com.ab.notification.exception.ErrorCode;
import com.ab.notification.helper.EmailHelper;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.repository.ErrorBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Below class will provide helper methods to process and save mail recipients details and mail details in DB for
 * it to be retried and processed later using scheduler.
 */
@Component
public class ErrorTrackingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorTrackingHelper.class);

    private final ErrorBatchRepository errorBatchRepository;

    private final EmailHelper emailHelper;

    @Autowired
    public ErrorTrackingHelper(ErrorBatchRepository errorRepository, EmailHelper helper) {
        errorBatchRepository = errorRepository;
        emailHelper = helper;
    }

    /**
     * This will save ErrorBatchEntity in DB
     *
     * @param errorBatchEntity ErrorBatchEntity
     */
    @Log
    public void saveErrorBatchDetails(ErrorBatchEntity errorBatchEntity) throws AppException {
        try {
            ErrorBatchEntity savedErrorEntity = errorBatchRepository.save(errorBatchEntity);
            LOGGER.error("Error batch emails saved in DB for ID {}", savedErrorEntity.getId());
        } catch (Exception e) {
            emailHelper.sendMailToAdmin(emailHelper.prepareMailMap(NotificationContants.MAIL_SUBJECT_FOR_ERROR_WHILE_SENDING_TO_ADMIN, String.format(NotificationContants.MAIL_BODY_FOR_ERROR_WHILE_SENDING_TO_ADMIN, errorBatchEntity.getErrorBatchDetail())), false);
        }
    }

}
