package com.ab.notification.service;

import com.ab.notification.annotation.Log;
import com.ab.notification.batch.listener.NewsletterJobListener;
import com.ab.notification.exception.AppException;
import com.ab.notification.exception.ErrorCode;
import com.ab.notification.helper.EmailHelper;
import com.ab.notification.helper.GlobalHelper;
import com.ab.notification.model.ErrorBatchEntity;
import com.ab.notification.user.UserClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * This class is general method for sending mails
 */
//TODO 5) Use Spring Batch for batch processing and see its advantages over written logic.
@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final int BATCH_SIZE = 50;

    private final ErrorTrackingHelper errorTrackingHelper;

    private final ExecutorService executorService;

    private EmailHelper emailHelper;

    private UserClient userClient;

    private GlobalHelper globalHelper;

    private final JobLauncher jobLauncher;

    private Job job;

    private final NewsletterJobListener newsletterJobListener;

    @Autowired
    public EmailService(ErrorTrackingHelper trackingHelper, EmailHelper helper, UserClient client, GlobalHelper globalHelper, @Qualifier("notificationJob") Job job, JobLauncher jobLauncher, NewsletterJobListener newsletterJobListener) {
        executorService = Executors.newFixedThreadPool(10);
        errorTrackingHelper = trackingHelper;
        emailHelper = helper;
        userClient = client;
        this.globalHelper = globalHelper;
        this.job = job;
        this.jobLauncher = jobLauncher;
        this.newsletterJobListener = newsletterJobListener;
    }

    @Log
    public Boolean sendMail(Map<String, String> mailMap, HttpServletRequest httpServletRequest) throws AppException {
//      Check mailTos if not present get data of users from DB calling auth service
        String[] mailTos;
        if (!mailMap.get("mailTo").isBlank()) {
            mailTos = mailMap.get("mailTo").split(",");
        } else {
            try {
                String jobID = UUID.randomUUID().toString();
                Map<String, Map<String, String>> jobMailMap = new HashMap<>();
                jobMailMap.put(jobID, mailMap);
                newsletterJobListener.setJobExecutionMailMaps(jobMailMap);
                JobParameters jobParameter = new JobParametersBuilder().
                        addJobParameter("jobId", jobID, String.class).
                        addJobParameter("timestamp", System.currentTimeMillis(), Long.class).
                        addJobParameter("isRetry", false, Boolean.class).
                        toJobParameters();
                jobLauncher.run(job, jobParameter);
                return true;
//              mailTos = userClient.getUserEmails(globalHelper.generateTokenViaSubjectForRestCall(NotificationContants.NOTIFICATION_SERVICE_NAME)).getBody();
            } catch (Exception e) {
                throw new AppException(ErrorCode.JOB_LAUNCH_ERROR, e.getMessage());
            }
        }

        try {
            if (mailTos.length > 0) {
                sendBatchMails(mailMap, mailTos, false);
                return true;
            }


/*
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
*/
        } catch (Exception e) {
            throw e;
        }
//        //Process error details
//        if (MAIL_SENDING_FAILED_BATCH.size() > 0) {
//            processErrorBatchDetails(mailMap);
//        }
        return true;
    }


    /**
     * sendMail() method sends mail to multiple users in batch via multiple threads
     *
     * @param mailMap Map
     * @return Boolean
     */
    @Log
    public void sendBatchMails(Map<String, String> mailMap, String[] mailTos, boolean isFromRetryer) throws AppException {
        ArrayList<String> mailSendingFailedBatch = new ArrayList<>();
        for (String mailTo : mailTos) {
            sendBatchMail(mailMap, mailTo, isFromRetryer, mailSendingFailedBatch);
        }
        //Process error details
        if (!isFromRetryer && !mailSendingFailedBatch.isEmpty()) {
            processErrorBatchDetails(mailMap, mailSendingFailedBatch);
        }
    }

    /**
     * sendMail() method sends mail to singer user
     *
     * @param mailMap Map
     * @return Boolean
     */
    @Log
    public void sendBatchMail(Map<String, String> mailMap, String mailTo, boolean isFromRetryer, ArrayList<String> mailSendingFailedBatch) throws AppException {
        try {
            LOGGER.debug("Sending Mail to {}", mailTo);
            emailHelper.sendMails(mailMap, mailTo);
        } catch (Exception e) {
            if (!isFromRetryer) {
                mailSendingFailedBatch.add(mailTo);
            }
            LOGGER.error("Exception in sendBatchMails()");
            if (isFromRetryer) {
                throw new AppException(ErrorCode.RETRY_SEND_EMAIL_ERROR, "Exception while retrying failed emails batches");
            }
        }
    }


    /**
     * process and save error details in DB
     *
     * @param mailMap Map
     */
    public void processErrorBatchDetails(Map<String, String> mailMap, ArrayList<String> mailSendingFailedBatch) throws AppException {
        ErrorBatchEntity errorBatchEntity = new ErrorBatchEntity();
        errorBatchEntity.setErrorBatchDetail(String.join(",", mailSendingFailedBatch));
        errorBatchEntity.setMailBody(mailMap.get("text"));
        errorBatchEntity.setMailSubject(mailMap.get("subject"));
        errorTrackingHelper.saveErrorBatchDetails(errorBatchEntity);
    }
}
