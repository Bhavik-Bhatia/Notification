package com.ab.notification.batch.config.job;

import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.repository.JobRepository;

@Configuration
public class NotificationJobConfiguration {

    private final JobRepository jobRepository;

    private final Step step;

    @Autowired
    public NotificationJobConfiguration(JobRepository jobRepository, @Autowired @Qualifier("newsletterStep") Step step) {
        this.jobRepository = jobRepository;
        this.step = step;
    }

    @Bean(name = "notificationJob")
    public Job job() {
        return new JobBuilder("notificationJob", jobRepository)
                .start(step)
                .build();
    }
}
