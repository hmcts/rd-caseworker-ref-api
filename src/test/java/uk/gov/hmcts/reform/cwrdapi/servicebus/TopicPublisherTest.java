package uk.gov.hmcts.reform.cwrdapi.servicebus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.IllegalStateException;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;

import java.net.NoRouteToHostException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TopicPublisherTest {
    @Mock
    PublishCaseWorkerData publishCaseWorkerData;

    private static final String DESTINATION = "Bermuda";
    private final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    private TopicPublisher topicPublisher = new TopicPublisher(jmsTemplate, DESTINATION);

    @Test
    public void sendMessageCallsTheJmsTemplate() {
        topicPublisher.sendMessage(publishCaseWorkerData);

        verify(jmsTemplate).convertAndSend(DESTINATION, publishCaseWorkerData);
    }

    @Test(expected = CaseworkerMessageFailedException.class)
    public void recoverMessageThrowsThePassedException() throws Throwable {
        Exception exception = new NoRouteToHostException("");
        topicPublisher.recoverMessage(exception);
    }

    @Test
    public void sendMessageWhenThrowExceptionWhenConnectionFactoryInstanceDifferent() {
        SingleConnectionFactory connectionFactory = mock(SingleConnectionFactory.class);
        doThrow(IllegalStateException.class).when(jmsTemplate).convertAndSend(DESTINATION, publishCaseWorkerData);

        topicPublisher = new TopicPublisher(jmsTemplate, DESTINATION);

        try {
            topicPublisher.sendMessage(publishCaseWorkerData);
        } catch (Exception e) {
            verify(connectionFactory, never()).resetConnection();
            verify(jmsTemplate, times(1)).convertAndSend(DESTINATION, publishCaseWorkerData);
        }
    }
}

