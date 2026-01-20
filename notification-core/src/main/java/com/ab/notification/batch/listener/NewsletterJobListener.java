package com.ab.notification.batch.listener;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
@Getter
public class NewsletterJobListener implements JobExecutionListener {

    private Map<String, Map<String, String>> jobExecutionMailMaps;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecutionMailMaps != null) {
            jobExecutionMailMaps.remove(jobExecution.getJobParameters().getParameter("jobId").getValue());
        }
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (jobExecutionMailMaps != null) {
            jobExecution.getExecutionContext().put("mailMap", jobExecutionMailMaps.get(jobExecution.getJobParameters().getParameter("jobId").getValue()));
        }
    }
}
