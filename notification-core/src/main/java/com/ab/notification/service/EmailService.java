package com.ab.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * This class is general method for sending mails
 */

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    private final Environment environment;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, Environment environment) {
        this.environment = environment;
        this.javaMailSender = javaMailSender;
    }

    /**
     * sendMail() method sends mail to multiple users.
     *
     * @param mailMap Map
     * @return Boolean
     */
    public Boolean sendMail(Map<String, String> mailMap) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            LOGGER.debug("Sending Mail to {}", mailMap.get("mailFrom"));
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, Boolean.parseBoolean(mailMap.get("isMultipart")));
            mimeMessageHelper.setSubject(mailMap.get("subject"));
            mimeMessageHelper.setFrom(new InternetAddress(environment.getProperty("spring.mail.username")));
            mimeMessageHelper.setTo(mailMap.get("mailTo").split(","));
//          todo: Send complete message in text, wherever sendMail is called from.
            mimeMessageHelper.setText(mailMap.get("text"));
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            return true;
        } catch (MessagingException e) {
            LOGGER.error("MessagingException while Sending Mail{}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
            return false;
        }
    }
}
