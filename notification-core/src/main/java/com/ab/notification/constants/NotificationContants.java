package com.ab.notification.constants;

public interface NotificationContants {

    String MAIL_SUBJECT_FOR_ERROR_WHILE_SENDING_TO_ADMIN = "Error while saving details";
    String MAIL_BODY_FOR_ERROR_WHILE_SENDING_TO_ADMIN = "Hi, an error occurred while saving details in DB, data of error batch entity is %s. Please check the reason.";
    String MAIL_SUBJECT_FOR_RETRYING_MORE_THAN_TWICE_SENDING_TO_ADMIN = "Retryer alert!";
    String MAIL_SUBJECT_FOR_RETRYING_FAILURE_ALERT_SENDING_TO_ADMIN = "Retryer failure alert!";
    String MAIL_BODY_FOR_RETRYING_MORE_THAN_TWICE_SENDING_TO_ADMIN = "Hi, following batches %s have been retried for more than twice. Please check why and fix the issue.";
    String MAIL_BODY_FOR_RETRYING_FAILURE_ALERT_SENDING_TO_ADMIN = "Hi, following batches %s have failed while retrying. Please check why and fix the issue.";
    String NOTIFICATION_SERVICE_NAME = "notification";
}
