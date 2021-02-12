package uk.gov.hmcts.reform.cwrdapi.servicebus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class TopicPublisher {

    private final JmsTemplate jmsTemplate;
    private final String destination;
    private final IValidationService validationService;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    public TopicPublisher(JmsTemplate jmsTemplate,
                          @Value("${crd.publisher.azure.service.bus.topic}") final String destination,
                          IValidationService validationService) {
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
        this.validationService = validationService;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public void sendMessage(@NotNull Object message) {
        log.info("{}:: Publishing message to service bus topic.", loggingComponentName);
        if (message instanceof PublishCaseWorkerData) {
            log.info("{}:: Job Id is: {}, Count of User Ids is: {} ",
                    loggingComponentName,
                    validationService.getAuditJobId(),
                    ((PublishCaseWorkerData) message).getUserIds() != null
                            ? ((PublishCaseWorkerData) message).getUserIds().size() : null);
        }

        jmsTemplate.convertAndSend(destination, message);
        log.info("{}:: Message published to service bus topic", loggingComponentName);
    }

    @Recover
    public void recoverMessage(Exception ex) {
        log.error("{}:: Publishing message to service bus topic failed with exception: {} ", loggingComponentName, ex);
        throw new CaseworkerMessageFailedException(ex.getMessage());
    }
}

