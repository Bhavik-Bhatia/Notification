package com.ab.notification.batch.reader;

import com.ab.notification.annotation.Log;
import com.ab.notification.dto.EmailDTO;
import com.ab.notification.helper.GlobalHelper;
import com.ab.notification.user.UserClient;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@StepScope
public class NewsletterEmailReader implements ItemReader<EmailDTO>, ItemStream {

    @Value("#{jobExecutionContext['mailMap']}")
    private Map<String, String> mailMap;

/*
    @Value("#{jobParameters['isRetry']}")
    private boolean isRetry;
*/


    private final UserClient userClient;
    private final GlobalHelper globalHelper;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private volatile String[] mailTos;

    @Autowired
    public NewsletterEmailReader(UserClient userClient, GlobalHelper globalHelper) {
        this.userClient = userClient;
        this.globalHelper = globalHelper;
    }

    @Override
    @Log
    public EmailDTO read() {
        if (currentIndex.get() == 0) {
            synchronized (this) {
                if (currentIndex.get() == 0) {
//                  mailTos = userClient.getUserEmails(globalHelper.generateTokenViaSubjectForRestCall(NotificationContants.NOTIFICATION_SERVICE_NAME)).getBody();
                    mailTos = new String[]{"bhavikbhatia9@gmail.com", "bhavikbhatia19@gmail.com"};
                    return new EmailDTO(mailMap, mailTos[currentIndex.getAndIncrement()], false);
                }
            }
        }

        if (currentIndex.get() < mailTos.length) {
            return new EmailDTO(mailMap, mailTos[currentIndex.getAndIncrement()], false);
        } else {
            currentIndex.set(0); // Reset for potential future reads
            return null;
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.currentIndex.set(executionContext.getInt("reader.index", 0));
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt("reader.index", this.currentIndex.get());
    }
}
