package uk.gov.hmcts.reform.cwrdapi.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.launchdarkly.shaded.com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Slf4j
@Service
public class TopicPublisher {

    private final IValidationService validationService;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${crd.publisher.caseWorkerDataPerMessage}")
    private int caseWorkerDataPerMessage;

    @Value("${crd.publisher.azure.service.bus.host}")
    String host;
    @Value("${crd.publisher.azure.service.bus.topic}")
    String topic;
    @Value("${crd.publisher.azure.service.bus.username}")
    String sharedAccessKeyName;
    @Value("${crd.publisher.azure.service.bus.password}")
    String sharedAccessKeyValue;

    @Autowired
    public TopicPublisher(IValidationService validationService) {
        this.validationService = validationService;
    }

    //The Azure library has retry mechanism inbuilt, which will try to send message until timeout.
    // There is no need for explicit retry mechanism.
    public void sendMessage(@NotNull PublishCaseWorkerData caseWorkerIds) {
        ServiceBusSenderClient serviceBusSenderClient = null;
        ServiceBusTransactionContext transactionContext = null;

        try {
            log.info("{}:: Publishing message to service bus topic:: Job Id is: {}", loggingComponentName,
                    validationService.getAuditJobId());

            log.info("{}:: Job Id is: {}, Count of User Ids is: {} ",
                    loggingComponentName, validationService.getAuditJobId(), caseWorkerIds.getUserIds().size());

            serviceBusSenderClient = getServiceBusSenderClient();
            transactionContext = serviceBusSenderClient.createTransaction();
            publishMessageToTopic(caseWorkerIds, serviceBusSenderClient, transactionContext);

        } catch (Exception exception) {
            log.error("{}:: Publishing message to service bus topic failed with exception: {}:: Job Id {}",
                    loggingComponentName, exception, validationService.getAuditJobId());
            if (Objects.nonNull(serviceBusSenderClient) && Objects.nonNull(transactionContext)) {
                serviceBusSenderClient.rollbackTransaction(transactionContext);
                serviceBusSenderClient.close();
            }
            throw new CaseworkerMessageFailedException(CaseWorkerConstants.ASB_PUBLISH_ERROR);
        }

        serviceBusSenderClient.commitTransaction(transactionContext);
        serviceBusSenderClient.close();
        log.info("{}:: Message published to service bus topic:: Job Id is: {}", loggingComponentName,
                validationService.getAuditJobId());
    }

    public void publishMessageToTopic(PublishCaseWorkerData caseWorkerData,
                                      ServiceBusSenderClient serviceBusSenderClient,
                                      ServiceBusTransactionContext transactionContext) {
        log.info("{}:: Started publishing to topic::", loggingComponentName);
        ListUtils.partition(caseWorkerData.getUserIds(), caseWorkerDataPerMessage)
                .forEach(data -> {
                    PublishCaseWorkerData publishCaseWorkerDataChunk = new PublishCaseWorkerData();
                    publishCaseWorkerDataChunk.setUserIds(data);

                    serviceBusSenderClient.sendMessage(
                            new ServiceBusMessage(new Gson().toJson(publishCaseWorkerDataChunk)),
                            transactionContext);
                });
    }

    public ServiceBusSenderClient getServiceBusSenderClient() {
        String connectionString = "Endpoint=sb://" +
                host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(new AmqpRetryOptions())
                .sender()
                .topicName(topic)
                .buildClient();
    }
}

