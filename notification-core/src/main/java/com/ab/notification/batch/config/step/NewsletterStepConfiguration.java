package com.ab.notification.batch.config.step;

import com.ab.notification.batch.reader.FailedEmailsReader;
import com.ab.notification.batch.reader.NewsletterEmailReader;
import com.ab.notification.batch.writer.FailedEmailsWriter;
import com.ab.notification.batch.writer.NewsletterEmailWriter;
import com.ab.notification.dto.EmailDTO;
import com.ab.notification.model.ErrorBatchEntity;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NewsletterStepConfiguration {

    private final JobRepository jobRepository;
    private final NewsletterEmailReader newsletterEmailReader;
    private final FailedEmailsReader failedEmailsReader;
    private final FailedEmailsWriter failedEmailsWriter;
    private final NewsletterEmailWriter newsletterEmailWriter;
    private final PlatformTransactionManager platformTransactionManager;
    private static final int CHUNK_SIZE = 2;

    @Autowired
    public NewsletterStepConfiguration(JobRepository jobRepository, NewsletterEmailReader newsletterEmailReader, NewsletterEmailWriter newsletterEmailWriter, PlatformTransactionManager platformTransactionManager, FailedEmailsReader failedEmailsReader, FailedEmailsWriter failedEmailsWriter) {
        this.newsletterEmailReader = newsletterEmailReader;
        this.newsletterEmailWriter = newsletterEmailWriter;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.failedEmailsReader = failedEmailsReader;
        this.failedEmailsWriter = failedEmailsWriter;
    }

    @Bean(name = "newsletterStep")
    public Step newsletterStep() {
        return new StepBuilder("newsletterStep", jobRepository).<EmailDTO, EmailDTO>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(newsletterEmailReader) //Reads user emails from user microservice and
//              .processor(newsLetterEmailProcessor) //As during retry processor is also rerun we are not able to maintain state of email DTO object so we do not retry success ones
                .writer(newsletterEmailWriter)
                .faultTolerant() //Enables retry skips
                .retryLimit(2)
                .retry(Exception.class)
                .taskExecutor(taskExecutor())  // Enable multithreading
                .build();
    }

    @Bean(name = "newsletterRetryStep")
    public Step newsletterRetryStep() {
        return new StepBuilder("newsletterRetryStep", jobRepository).<ErrorBatchEntity, ErrorBatchEntity>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(failedEmailsReader) //Reads Error Batch Entities from DB
                .writer(failedEmailsWriter)
                .faultTolerant() //Enables retry skips
                .retryLimit(1)
                .retry(Exception.class)
                .skipLimit(Integer.MAX_VALUE) //How many items allowed to skip
                .skip(Exception.class)
                .taskExecutor(retryTaskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("newsletter-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor retryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("newsletter-retry-thread-");
        executor.initialize();
        return executor;
    }

}
