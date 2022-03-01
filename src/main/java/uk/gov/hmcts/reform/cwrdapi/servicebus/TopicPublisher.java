package uk.gov.hmcts.reform.cwrdapi.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
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

    @Autowired
    private IValidationService validationService;
    @Autowired
    private ServiceBusSenderClient serviceBusSenderClient;

    @Value("${loggingComponentName}")
    String loggingComponentName;

    @Value("${crd.publisher.caseWorkerDataPerMessage}")
    int caseWorkerDataPerMessage;

    @Value("${crd.publisher.azure.service.bus.topic}")
    String topic;

    public void sendMessage(@NotNull List<String> caseWorkerIds) {
        ServiceBusTransactionContext transactionContext = null;

        try {
            log.info("{}:: Publishing message to service bus topic:: Job Id is: {}", loggingComponentName,
                    validationService.getAuditJobId());

            log.info("{}:: Job Id is: {}, Count of User Ids is: {} ",
                    loggingComponentName, validationService.getAuditJobId(), caseWorkerIds.size());

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

    private void publishMessageToTopic(List<String> caseWorkerData,
                                       ServiceBusSenderClient serviceBusSenderClient,
                                       ServiceBusTransactionContext transactionContext) {
        log.info("{}:: Started publishing to topic:: Job Id {}", loggingComponentName,
                validationService.getAuditJobId());
        ServiceBusMessageBatch messageBatch = serviceBusSenderClient.createMessageBatch();
        List<ServiceBusMessage> serviceBusMessages = new ArrayList<>();

        ListUtils.partition(caseWorkerData, caseWorkerDataPerMessage)
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
                log.error("{}:: Message is too large for an empty batch. Skipping. Max size: {}. Job id::{}",
                        loggingComponentName, messageBatch.getMaxSizeInBytes(), validationService.getAuditJobId());
            }
        }

        if (messageBatch.getCount() > 0) {
            serviceBusSenderClient.sendMessages(messageBatch, transactionContext);
            log.info("{}:: Sent a batch of messages count: {} to the topic: {} ::Job id::{}", loggingComponentName,
                    messageBatch.getCount(),topic,
                    validationService.getAuditJobId());
        }
    }

}

