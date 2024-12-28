package com.ab.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is general method for sending mails
 */

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final int BATCH_SIZE = 50;

    private final JavaMailSender javaMailSender;

    private final Environment environment;

    private final String MAIL_FROM;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, Environment environment) {
        this.environment = environment;
        this.javaMailSender = javaMailSender;
        MAIL_FROM = environment.getProperty("spring.mail.username");
    }

    public Boolean sendMail(Map<String, String> mailMap) {
        String[] mailTos = mailMap.get("mailTo").split(",");
        final double NUMBER_OF_BATCHES = Math.ceil((double) mailTos.length / BATCH_SIZE);

//      In case batch size is one we avoid all calculation and processing
        if (NUMBER_OF_BATCHES == 1) {
            sendBatchMails(mailMap, mailTos);
        }

        try (ExecutorService executorService = Executors.newFixedThreadPool((int) NUMBER_OF_BATCHES)) {
            for (int incrementalNumOfBatches = 0; incrementalNumOfBatches < NUMBER_OF_BATCHES; incrementalNumOfBatches++) {
                int startIndex = incrementalNumOfBatches * BATCH_SIZE;
                int endIndex = startIndex + BATCH_SIZE;

//              In case if last batch is not equal to 50
                if (endIndex > mailTos.length) {
                    endIndex = mailTos.length;
                }

                String[] srcMailTos = Arrays.copyOfRange(mailTos, startIndex, endIndex);
                executorService.submit(() -> sendBatchMails(mailMap, srcMailTos));
            }
        } catch (Exception e) {
            LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * sendMail() method sends mail to multiple users in batch via multiple threads
     *
     * @param mailMap Map
     * @return Boolean
     */
    public void sendBatchMails(Map<String, String> mailMap, String[] mailTos) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            LOGGER.debug("Sending Mail to {}", MAIL_FROM);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, Boolean.parseBoolean(mailMap.get("isMultipart")));
            mimeMessageHelper.setSubject(mailMap.get("subject"));
            mimeMessageHelper.setFrom(Objects.requireNonNull(MAIL_FROM), Objects.requireNonNull(environment.getProperty("spring.mail.display.name")));
            mimeMessageHelper.setBcc(mailTos);
            mimeMessageHelper.setText(mailMap.get("text"));
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            LOGGER.error("MessagingException while Sending Mail{}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
        }
    }
}
