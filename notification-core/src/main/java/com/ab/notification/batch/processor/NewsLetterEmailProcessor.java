package com.ab.notification.batch.processor;

import com.ab.notification.dto.EmailDTO;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@StepScope
public class NewsLetterEmailProcessor implements ItemProcessor<String, EmailDTO>, StepExecutionListener {

    private Map<String, String> mailMap;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.mailMap = (Map<String, String>) stepExecution.getJobExecution().getExecutionContext().get("mailMap");
    }

    @Override
    public EmailDTO process(String email) {
        return new EmailDTO(mailMap, email); // combine
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
