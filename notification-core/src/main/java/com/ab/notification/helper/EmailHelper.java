package com.ab.notification.helper;

import com.ab.notification.actuator.metrics.MailMetrics;
import com.ab.notification.annotation.Log;
import io.micrometer.core.instrument.Counter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class contains helper methods for mail related activities
 */
@Component
public class EmailHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailHelper.class);

    private final JavaMailSender javaMailSender;

    private final Environment environment;

    private final String MAIL_FROM;

    private final String[] ADMIN_MAILS;

    private final MailMetrics mailMetrics;

    @Autowired
    public EmailHelper(JavaMailSender mailSender, Environment env, MailMetrics mailMetrics) {
        javaMailSender = mailSender;
        environment = env;
        this.mailMetrics = mailMetrics;
        ADMIN_MAILS = environment.getProperty("admin.emails").split(",");
        MAIL_FROM = environment.getProperty("spring.mail.username");
    }

    @Log
    public void sendMails(Map<String, String> mailMap, String mailTo) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, Boolean.parseBoolean(mailMap.get("isMultipart")));
        mimeMessageHelper.setSubject(mailMap.get("subject"));
        mimeMessageHelper.setFrom(Objects.requireNonNull(MAIL_FROM), Objects.requireNonNull(environment.getProperty("spring.mail.display.name")));
        mimeMessageHelper.setTo(mailTo);
        mimeMessageHelper.setText(mailMap.get("text"));
        javaMailSender.send(mimeMessageHelper.getMimeMessage());
        mailMetrics.incrementMailSenderCounter();
    }

    /**
     * This method will be used to send mail to admins in case a failure occurred while retrying or we are retrying for more than 2 times, etc.
     *
     * @param mailMap       Map
     * @param isFromRetryer boolean
     */
    @Log
    public void sendMailToAdmin(Map<String, String> mailMap, boolean isFromRetryer) {
        for (String adminMail : ADMIN_MAILS) {
            try {
                sendMails(mailMap, adminMail);
            } catch (Exception e) {
                LOGGER.debug("Exception while sending mail to admin");
            }
        }
    }

    @Log
    public void sendMailToAdminForHealth(Map<String, String> mailMap, boolean isForHealth) throws MessagingException, UnsupportedEncodingException {
        for (String adminMail : ADMIN_MAILS) {
            try {
                sendMails(mailMap, adminMail);
            } catch (Exception e) {
                if (isForHealth) {
                    throw e;
                }
            }
        }
    }


    public Map<String, String> prepareMailMap(String subject, String body) {
        Map<String, String> map = new HashMap<>();
        map.put("subject", subject);
        map.put("text", body);
        return map;
    }
}
