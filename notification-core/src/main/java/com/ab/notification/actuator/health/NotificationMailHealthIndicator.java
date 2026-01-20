package com.ab.notification.actuator.health;

import com.ab.notification.helper.EmailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Counter for send mails health by: /actuator/health/notificationMail
 */
@Component
public class NotificationMailHealthIndicator implements HealthIndicator {

    private final EmailHelper emailHelper;

    @Autowired
    public NotificationMailHealthIndicator(EmailHelper emailHelper) {
        this.emailHelper = emailHelper;
    }

    @Override
    public Health health() {
        try {
            Map<String, String> details = new HashMap<>();
            details.put("subject", "Test Mail");
            details.put("text", "Test Mail Body For Health Check");
            details.put("isMultipart", "false");
            emailHelper.sendMailToAdminForHealth(details, true);
            return Health.up().withDetail("smtp", "Available").build();
        }catch (Exception e){
            return Health.down().withDetail("smtp", "Not Available").build();
        }
    }
}
