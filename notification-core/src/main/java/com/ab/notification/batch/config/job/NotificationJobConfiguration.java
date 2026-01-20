package com.ab.notification.batch.config.job;

import com.ab.notification.batch.listener.NewsletterJobListener;
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

    private final NewsletterJobListener newsletterJobListener;

    @Autowired
    public NotificationJobConfiguration(JobRepository jobRepository, NewsletterJobListener newsletterJobListener) {
        this.jobRepository = jobRepository;
        this.newsletterJobListener = newsletterJobListener;
    }

    @Bean(name = "notificationJob")
    public Job job(@Qualifier("newsletterStep") Step step) {
        return new JobBuilder("notificationJob", jobRepository)
                .start(step)
                .listener(newsletterJobListener)
                .build();
    }

    @Bean(name = "notificationRetryJob")
    public Job retryJob(@Qualifier("newsletterRetryStep") Step step) {
        return new JobBuilder("notificationRetryJob", jobRepository)
                .start(step)
                .listener(newsletterJobListener)
                .build();
    }

}
