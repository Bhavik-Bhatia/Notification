package com.ab.notification.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    private final JavaMailSender javaMailSender;

    private final Environment environment;

    private final String MAIL_FROM;

    private final ExecutorService executorService;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, Environment environment) {
        this.environment = environment;
        this.javaMailSender = javaMailSender;
        MAIL_FROM = environment.getProperty("spring.mail.username");
        executorService = Executors.newFixedThreadPool(10);
    }

    public Boolean sendMail(Map<String, String> mailMap, HttpServletRequest httpServletRequest) {
        String[] mailTos = mailMap.get("mailTo").split(",");
        final double NUMBER_OF_BATCHES = Math.ceil((double) mailTos.length / BATCH_SIZE);
        List<Callable<Void>> batchTasks = new ArrayList<>();

//      In case batch size is one we avoid all calculation and processing
        if (NUMBER_OF_BATCHES == 1) {
            sendBatchMails(mailMap, mailTos);
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
                    sendBatchMails(mailMap, srcMailTos);
                    return null;
                });
            }
            List<Future<Void>> futures = executorService.invokeAll(batchTasks);
            for (Future<Void> future : futures) {
                future.get();
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
        try {
            LOGGER.debug("Sending Mail to {}", MAIL_FROM);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, Boolean.parseBoolean(mailMap.get("isMultipart")));
            mimeMessageHelper.setSubject(mailMap.get("subject"));
            mimeMessageHelper.setFrom(Objects.requireNonNull(MAIL_FROM), Objects.requireNonNull(environment.getProperty("spring.mail.display.name")));
            mimeMessageHelper.setBcc(mailTos);
            mimeMessageHelper.setText(mailMap.get("text"));
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (Exception e) {
            LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
        }
    }
}
