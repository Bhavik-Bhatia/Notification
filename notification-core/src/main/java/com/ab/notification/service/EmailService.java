package com.ab.notification.service;

import com.ab.notification.helper.EmailHelper;
import com.ab.notification.model.ErrorBatchEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * This class is general method for sending mails
 */

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final int BATCH_SIZE = 50;

    private final ErrorTrackingHelper errorTrackingHelper;

    private ArrayList<String> MAIL_SENDING_FAILED_BATCH = new ArrayList<>();

    private final ExecutorService executorService;

    private EmailHelper emailHelper;

    @Autowired
    public EmailService(ErrorTrackingHelper trackingHelper, EmailHelper helper) {
        executorService = Executors.newFixedThreadPool(10);
        errorTrackingHelper = trackingHelper;
        emailHelper = helper;
    }

    public Boolean sendMail(Map<String, String> mailMap, HttpServletRequest httpServletRequest) {
        String[] mailTos = mailMap.get("mailTo").split(",");
        final double NUMBER_OF_BATCHES = Math.ceil((double) mailTos.length / BATCH_SIZE);
        List<Callable<Void>> batchTasks = new ArrayList<>();

//      In case batch size is one we avoid all calculation and processing
        if (NUMBER_OF_BATCHES == 1) {
            sendBatchMails(mailMap, mailTos, false);
//          Process error details
            if (MAIL_SENDING_FAILED_BATCH.size() > 0) {
                processErrorBatchDetails(mailMap);
            }
            return true;
        }
        try {
            for (int incrementalNumOfBatches = 0; incrementalNumOfBatches < NUMBER_OF_BATCHES; incrementalNumOfBatches++) {
                int startIndex = incrementalNumOfBatches * BATCH_SIZE;
                int endIndex = startIndex + BATCH_SIZE;

//              In case if last batch is not equal to 50
                if (endIndex > mailTos.length) {
                    endIndex = mailTos.length;
                }

                String[] srcMailTos = Arrays.copyOfRange(mailTos, startIndex, endIndex);
                batchTasks.add(() -> {
                    sendBatchMails(mailMap, srcMailTos, false);
                    return null;
                });
            }
            List<Future<Void>> futures = executorService.invokeAll(batchTasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            LOGGER.error("Exception in Sending Mail{}", e.getMessage());
            return false;
        }
//      Process error details
        if (MAIL_SENDING_FAILED_BATCH.size() > 0) {
            processErrorBatchDetails(mailMap);
        }
        return true;
    }


    /**
     * sendMail() method sends mail to multiple users in batch via multiple threads
     *
     * @param mailMap Map
     * @return Boolean
     */
    public void sendBatchMails(Map<String, String> mailMap, String[] mailTos, boolean isFromRetryer) {
        for (String mailTo : mailTos) {
            try {
                LOGGER.debug("Sending Mail to {}", mailTo);
                emailHelper.sendMails(mailMap, mailTo);
            } catch (Exception e) {
                if (!isFromRetryer) {
                    MAIL_SENDING_FAILED_BATCH.add(mailTo);
                }
                LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
                if (isFromRetryer){
                    throw new RuntimeException("Exception while retrying failed emails batches");
                }
            }
        }
    }

    /**
     * process and save error details in DB
     *
     * @param mailMap Map
     */
    private void processErrorBatchDetails(Map<String, String> mailMap) {
        ErrorBatchEntity errorBatchEntity = new ErrorBatchEntity();
        errorBatchEntity.setErrorBatchDetail(String.join(",", MAIL_SENDING_FAILED_BATCH));
        errorBatchEntity.setMailBody(mailMap.get("text"));
        errorBatchEntity.setMailSubject(mailMap.get("subject"));
        errorTrackingHelper.saveErrorBatchDetails(errorBatchEntity);
    }
}
