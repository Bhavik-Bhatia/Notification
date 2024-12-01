package com.ab.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationApplication.class);


    public static void main(String[] args) throws Exception {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
