package uk.gov.hmcts.reform.cwrdapi.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;

@Slf4j
@Service
//@EnableJms
//@EnableTransactionManagement
public class TopicPublisher {

    private final JmsTemplate jmsTemplate;
    private final String destination;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    public TopicPublisher(JmsTemplate jmsTemplate,
                          @Value("${crd.publisher.azure.service.bus.topic}") final String destination) {
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
    }

    //@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 3))
    //@Transactional
    public void sendMessage(Object message) {
        log.info("{}:: Publishing message to service bus topic.", loggingComponentName);
        jmsTemplate.convertAndSend(destination, message);
        log.info("{}:: Message published to service bus topic", loggingComponentName);
    }

    @Recover
    public void recoverMessage(Exception ex) {
        log.error("{}:: Publishing message to service bus topic failed with exception: {} ", loggingComponentName, ex);
        throw new CaseworkerMessageFailedException(ex.getMessage());
    }
}

