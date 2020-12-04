package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class TopicConsumer {

    private final Integer maxRetryAttempts;

    public TopicConsumer(
            @Value("${send-letter.maxRetryAttempts}") Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    @JmsListener(destination = "${amqp.topic}", containerFactory = "topicJmsListenerContainerFactory",
            subscription = "${amqp.subscription}")

    public void onMessage(String message) {
        processMessageWithRetry(message, 1);
    }

    private void processMessageWithRetry(String message, int retry) {
        try {
            processMessage(message);
        } catch (Exception e) {
        }
    }

    private void processMessage(String message) {
    }

}

