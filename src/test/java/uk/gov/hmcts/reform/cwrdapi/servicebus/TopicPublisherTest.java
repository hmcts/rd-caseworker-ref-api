
package uk.gov.hmcts.reform.cwrdapi.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicPublisherTest {

    @Mock
    IValidationService validationService;

    private final ServiceBusSenderClient serviceBusSenderClient = mock(ServiceBusSenderClient.class);
    private final ServiceBusTransactionContext transactionContext = mock(ServiceBusTransactionContext.class);
    private final ServiceBusMessageBatch messageBatch = mock(ServiceBusMessageBatch.class);

    @InjectMocks
    private TopicPublisher topicPublisher;

    PublishCaseWorkerData publishCaseWorkerData;
    List<String> userIdList;
    List<ServiceBusMessage> serviceBusMessageList = new ArrayList<>();

    @BeforeEach
    void beforeTest() {
        publishCaseWorkerData = new PublishCaseWorkerData();
        userIdList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            userIdList.add(UUID.randomUUID().toString());
        }
        publishCaseWorkerData.setUserIds(userIdList);
        serviceBusMessageList.add(new ServiceBusMessage(new Gson().toJson(publishCaseWorkerData)));

        topicPublisher.caseWorkerDataPerMessage = 2;
        topicPublisher.loggingComponentName = "loggingComponent";
        topicPublisher.topic = "dummyTopic";
    }

    @Test
    void sendMessageCallsAzureSendMessage() {

        doReturn(1L).when(validationService).getAuditJobId();
        doReturn(true).when(messageBatch).tryAddMessage(any());
        doReturn(1).when(messageBatch).getCount();
        doReturn(messageBatch).when(serviceBusSenderClient).createMessageBatch();

        topicPublisher.sendMessage(userIdList);

        verify(serviceBusSenderClient, times(1)).commitTransaction(any());
    }

    @Test
    void shouldThrowExceptionForConnectionIssues() {

        doReturn(1L).when(validationService).getAuditJobId();
        doReturn(transactionContext).when(serviceBusSenderClient).createTransaction();
        doThrow(new RuntimeException("Some Exception")).when(serviceBusSenderClient).createMessageBatch();
        Assertions.assertThrows(CaseworkerMessageFailedException.class, () ->
            topicPublisher.sendMessage(userIdList));
        verify(serviceBusSenderClient, times(1)).rollbackTransaction(any());
    }

    @Test
    void sendLargeMessageCallsAzureSendMessage() {

        doReturn(1L).when(validationService).getAuditJobId();
        doReturn(1).when(messageBatch).getCount();
        lenient().doReturn(false).when(messageBatch).tryAddMessage(any());
        doReturn(messageBatch).when(serviceBusSenderClient).createMessageBatch();

        topicPublisher.sendMessage(userIdList);

        verify(serviceBusSenderClient, times(1)).commitTransaction(any());
    }
}


