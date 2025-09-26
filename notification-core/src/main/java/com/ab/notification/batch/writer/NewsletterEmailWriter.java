package com.ab.notification.batch.writer;

import com.ab.notification.dto.EmailDTO;
import com.ab.notification.helper.EmailHelper;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsletterEmailWriter implements ItemWriter<EmailDTO> {


    private final EmailHelper emailHelper;

    @Autowired
    public NewsletterEmailWriter(EmailHelper emailHelper) {
        this.emailHelper = emailHelper;
    }

    @Override
    public void write(Chunk<? extends EmailDTO> chunk) throws Exception {
        for (EmailDTO emailDTO : chunk.getItems()) {
            emailHelper.sendMails(emailDTO.getMailMap(), emailDTO.getMailTo());
        }
    }
}

