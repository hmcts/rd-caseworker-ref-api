package uk.gov.hmcts.reform.cwrdapi.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.launchdarkly.shaded.com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.config.MessagingConfig;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class TopicPublisher {

    private final IValidationService validationService;
    private final MessagingConfig messagingConfig;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${crd.publisher.caseWorkerDataPerMessage}")
    private int caseWorkerDataPerMessage;

    @Value("${crd.publisher.azure.service.bus.topic}")
    private String topic;

    @Autowired
    public TopicPublisher(IValidationService validationService, MessagingConfig messagingConfig) {
        this.validationService = validationService;
        this.messagingConfig = messagingConfig;
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

            serviceBusSenderClient = messagingConfig.getServiceBusSenderClient();
            transactionContext = serviceBusSenderClient.createTransaction();
            publishMessageToTopic(caseWorkerIds, serviceBusSenderClient, transactionContext);
        } catch (Exception exception) {
            log.error("{}:: Publishing message to service bus topic failed with exception: {}:: Job Id {}",
                    loggingComponentName, exception, validationService.getAuditJobId());
            if (Objects.nonNull(serviceBusSenderClient) && Objects.nonNull(transactionContext)) {
                serviceBusSenderClient.rollbackTransaction(transactionContext);
            }
            throw new CaseworkerMessageFailedException(CaseWorkerConstants.ASB_PUBLISH_ERROR);
        }
        serviceBusSenderClient.commitTransaction(transactionContext);
        log.info("{}:: Message published to service bus topic:: Job Id is: {}", loggingComponentName,
                validationService.getAuditJobId());
    }

    public void publishMessageToTopic(PublishCaseWorkerData caseWorkerData,
                                      ServiceBusSenderClient serviceBusSenderClient,
                                      ServiceBusTransactionContext transactionContext) {
        log.info("{}:: Started publishing to topic::", loggingComponentName);
        ServiceBusMessageBatch messageBatch = serviceBusSenderClient.createMessageBatch();
        List<ServiceBusMessage> serviceBusMessages = new ArrayList<>();

        ListUtils.partition(caseWorkerData.getUserIds(), caseWorkerDataPerMessage)
                .forEach(data -> {
                    PublishCaseWorkerData publishCaseWorkerDataChunk = new PublishCaseWorkerData();
                    publishCaseWorkerDataChunk.setUserIds(data);
                    serviceBusMessages.add(new ServiceBusMessage(new Gson().toJson(publishCaseWorkerDataChunk)));
                });

        for (ServiceBusMessage message : serviceBusMessages) {
            if (messageBatch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            serviceBusSenderClient.sendMessages(messageBatch, transactionContext);

            // create a new batch
            messageBatch = serviceBusSenderClient.createMessageBatch();

            // Add that message that we couldn't before.
            if (!messageBatch.tryAddMessage(message)) {
                log.error("Message is too large for an empty batch. Skipping. Max size: {}.",
                        messageBatch.getMaxSizeInBytes());
            }
        }

        if (messageBatch.getCount() > 0) {
            serviceBusSenderClient.sendMessages(messageBatch, transactionContext);
            log.info("Sent a batch of messages to the topic: {}", topic);
        }
    }


}

