package com.ab.notification.batch.reader;

import com.ab.notification.constants.NotificationContants;
import com.ab.notification.helper.GlobalHelper;
import com.ab.notification.user.UserClient;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsletterEmailReader implements ItemReader<String> {

    private final UserClient userClient;
    private final GlobalHelper globalHelper;
    private int currentIndex = 0;
    private String[] mailTos;

    @Autowired
    public NewsletterEmailReader(UserClient userClient, GlobalHelper globalHelper) {
        this.userClient = userClient;
        this.globalHelper = globalHelper;
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentIndex == 0) {
            mailTos = userClient.getUserEmails(globalHelper.generateTokenViaSubjectForRestCall(NotificationContants.NOTIFICATION_SERVICE_NAME)).getBody();
            return mailTos[currentIndex++];
        } else if (currentIndex < mailTos.length) {
            return mailTos[currentIndex++];
        } else {
            currentIndex = 0; // Reset for potential future reads
            return null;
        }
    }
}
