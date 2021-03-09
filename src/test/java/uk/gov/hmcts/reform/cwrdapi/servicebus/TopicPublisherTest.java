
package uk.gov.hmcts.reform.cwrdapi.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.launchdarkly.shaded.com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.config.MessagingConfig;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class TopicPublisherTest {

    @Mock
    IValidationService validationService;

    @Mock
    MessagingConfig messagingConfig;

    private final ServiceBusSenderClient serviceBusSenderClient = mock(ServiceBusSenderClient.class);
    private final ServiceBusTransactionContext transactionContext = mock(ServiceBusTransactionContext.class);
    private final ServiceBusMessageBatch messageBatch = mock(ServiceBusMessageBatch.class);

    private TopicPublisher topicPublisher;
    PublishCaseWorkerData publishCaseWorkerData;
    List<ServiceBusMessage> serviceBusMessageList = new ArrayList<>();

    @Before
    public void beforeTest() {
        publishCaseWorkerData = new PublishCaseWorkerData();
        List<String> userIdList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            userIdList.add(UUID.randomUUID().toString());
        }
        publishCaseWorkerData.setUserIds(userIdList);
        serviceBusMessageList.add(new ServiceBusMessage(new Gson().toJson(publishCaseWorkerData)));
        topicPublisher = new TopicPublisher(validationService, messagingConfig);
        setField(topicPublisher, "caseWorkerDataPerMessage", 2);
        setField(topicPublisher, "topic", "dummyTopic");
    }

    @Test
    public void sendMessageCallsAzureSendMessage() {

        doReturn(1L).when(validationService).getAuditJobId();
        doReturn(serviceBusSenderClient).when(messagingConfig).getServiceBusSenderClient();
        doReturn(true).when(messageBatch).tryAddMessage(any());
        doReturn(1).when(messageBatch).getCount();
        doReturn(messageBatch).when(serviceBusSenderClient).createMessageBatch();

        topicPublisher.sendMessage(publishCaseWorkerData);

        verify(serviceBusSenderClient, times(1)).commitTransaction(any());
    }

    @Test(expected = CaseworkerMessageFailedException.class)
    public void shouldThrowExceptionForConnectionIssues() {

        doReturn(1L).when(validationService).getAuditJobId();
        doReturn(serviceBusSenderClient).when(messagingConfig).getServiceBusSenderClient();
        doReturn(transactionContext).when(serviceBusSenderClient).createTransaction();
        doThrow(new RuntimeException("Some Exception")).when(serviceBusSenderClient).createMessageBatch();

        topicPublisher.sendMessage(publishCaseWorkerData);
        verify(serviceBusSenderClient, times(1)).rollbackTransaction(any());
    }
}


