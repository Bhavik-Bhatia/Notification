package com.ab.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

//TODO 7: Use AOP and make annotations for Logging, Exception Handling, Security etc. Do not repeat code.
@SpringBootApplication(scanBasePackages = "com.ab")
@EnableAutoConfiguration
@EnableScheduling
@EnableFeignClients
public class NotificationApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationApplication.class);


    public static void main(String[] args) throws Exception {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
