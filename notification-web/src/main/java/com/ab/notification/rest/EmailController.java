package com.ab.notification.rest;

import com.ab.notification.annotation.Log;
import com.ab.notification.constants.NotificationURI;
import com.ab.notification.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class EmailController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * This API sends mail based on mail parameters passed in it.
     *
     * @return ResponseEntity<Boolean>
     */
    @PostMapping(value = NotificationURI.SEND_EMAIL_URI, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Log
    public ResponseEntity<Boolean> sendEmail(@NotNull @RequestParam Map<String, String> mailParam, HttpServletRequest httpServletRequest) throws Exception {
        Boolean response = emailService.sendMail(mailParam, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
