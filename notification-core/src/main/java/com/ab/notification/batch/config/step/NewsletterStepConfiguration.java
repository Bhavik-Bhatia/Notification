package com.ab.notification.batch.config.step;

import com.ab.notification.batch.processor.NewsLetterEmailProcessor;
import com.ab.notification.batch.reader.NewsletterEmailReader;
import com.ab.notification.batch.writer.NewsletterEmailWriter;
import com.ab.notification.dto.EmailDTO;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NewsletterStepConfiguration {

    private final JobRepository jobRepository;
    private final NewsletterEmailReader newsletterEmailReader;
    private final NewsletterEmailWriter newsletterEmailWriter;
    private final NewsLetterEmailProcessor newsLetterEmailProcessor;
    private final PlatformTransactionManager platformTransactionManager;
    private static final int CHUNK_SIZE = 5;

    @Autowired
    public NewsletterStepConfiguration(JobRepository jobRepository, NewsletterEmailReader newsletterEmailReader, NewsletterEmailWriter newsletterEmailWriter, PlatformTransactionManager platformTransactionManager, NewsLetterEmailProcessor newsLetterEmailProcessor) {
        this.newsletterEmailReader = newsletterEmailReader;
        this.newsletterEmailWriter = newsletterEmailWriter;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.newsLetterEmailProcessor = newsLetterEmailProcessor;
    }

    @Bean(name = "newsletterStep")
    public Step newsletterStep() {
        return new StepBuilder("newsletterStep", jobRepository).<String, EmailDTO>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(newsletterEmailReader) //Reads user emails from user microservice and
                .processor(newsLetterEmailProcessor)
                .writer(newsletterEmailWriter)
//              .faultTolerant()
//              .retryLimit(2)
                .build();
    }
}
