package uk.gov.hmcts.reform.cwrdapi.servicebus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.IllegalStateException;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.reform.cwrdapi.client.domain.TopicCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CaseworkerMessageFailedException;

import java.net.NoRouteToHostException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TopicPublisherTest {
    @Mock
    TopicCaseWorkerData topicCaseWorkerData;

    private static final String DESTINATION = "Bermuda";
    private final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    private final CachingConnectionFactory connectionFactory = mock(CachingConnectionFactory.class);
    private TopicPublisher topicPublisher = new TopicPublisher(jmsTemplate, DESTINATION, connectionFactory);

    @Test
    public void sendMessageCallsTheJmsTemplate() {
        topicPublisher.sendMessage(topicCaseWorkerData);

        verify(jmsTemplate).convertAndSend(DESTINATION, topicCaseWorkerData);
    }

    @Test(expected = NoRouteToHostException.class)
    public void recoverMessageThrowsThePassedException() throws Throwable {
        Exception exception = new NoRouteToHostException("");
        topicPublisher.recoverMessage(exception);
    }

    @Test(expected = CaseworkerMessageFailedException.class)
    public void sendMessageWhenThrowException() {

        doThrow(IllegalStateException.class).when(jmsTemplate).convertAndSend(DESTINATION,topicCaseWorkerData);

        topicPublisher.sendMessage(topicCaseWorkerData);
    }

    @Test
    public void sendMessageWhenThrowExceptionWhenConnectionFactoryInstanceDifferent() {
        SingleConnectionFactory connectionFactory = mock(SingleConnectionFactory.class);
        doThrow(IllegalStateException.class).when(jmsTemplate).convertAndSend(DESTINATION, topicCaseWorkerData);

        topicPublisher = new TopicPublisher(jmsTemplate, DESTINATION, connectionFactory);

        try {
            topicPublisher.sendMessage(topicCaseWorkerData);
        } catch (Exception e) {
            verify(connectionFactory, never()).resetConnection();
            verify(jmsTemplate, times(1)).convertAndSend(DESTINATION, topicCaseWorkerData);
        }

    }

    @Test(expected = Exception.class)
    public void sendMessageWhenOtherThrowException() {
        doThrow(Exception.class).when(jmsTemplate).send(anyString(), any());

        topicPublisher.sendMessage("a message");
    }
}

