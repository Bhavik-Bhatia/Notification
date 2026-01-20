package com.ab.notification.batch.writer;

import com.ab.notification.annotation.Log;
import com.ab.notification.dto.EmailDTO;
import com.ab.notification.service.EmailService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@StepScope
public class NewsletterEmailWriter implements ItemWriter<EmailDTO> {


    private final EmailService emailService;

    @Autowired
    public NewsletterEmailWriter(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @Log
    public void write(Chunk<? extends EmailDTO> chunk) throws Exception {
        ArrayList<String> mailSendingFailedBatch = new ArrayList<>();
        for (EmailDTO emailDTO : chunk.getItems()) {
            if (!emailDTO.isProcessedByWriter()) {
                emailService.sendBatchMail(emailDTO.getMailMap(), emailDTO.getMailTo(), false, mailSendingFailedBatch);
//              So on any Item failure from Chunk We Retry only failed ones
                emailDTO.setProcessedByWriter(true);
            }
        }
        if (!mailSendingFailedBatch.isEmpty()) {
            emailService.processErrorBatchDetails(chunk.getItems().getFirst().getMailMap(), mailSendingFailedBatch);
        }
    }
}

