/*
package uk.gov.hmcts.reform.cwrdapi.servicebus.messaging;

import org.junit.Test;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MessagingConfigTest {

    private final MessagingConfig messagingConfig = new MessagingConfig();
    ConnectionFactory connectionFactoryMock = mock(ConnectionFactory.class);
    SSLContext sslContextMock = mock(SSLContext.class);

    @Test
    public void testJmsUrlStringFormatsTheAmqpString() {
        final String url = messagingConfig.jmsUrlString("myHost");
        assertTrue("Jms url string should begin with amqps://<host> ", url.startsWith("amqps://myHost?"));
    }

    @Test
    public void testJmsConnectionFactoryTrustAllCertsFalse() {
        ConnectionFactory connectionFactory =
                messagingConfig.jmsConnectionFactory("clientId", "username",
                        "password", "jmsUrlString", sslContextMock, false);

        assertThat(connectionFactory).isNotNull();
    }

    @Test
    public void testJmsConnectionFactoryTrustAllCertsTrue() {
        ConnectionFactory connectionFactory =
                messagingConfig.jmsConnectionFactory("clientId", "username",
                        "password", "jmsUrlString", sslContextMock, true);

        assertThat(connectionFactory).isNotNull();
    }

    @Test
    public void testJmsTemplate() {
        JmsTemplate jmsTemplate = messagingConfig.jmsTemplate(connectionFactoryMock);
        assertEquals(jmsTemplate.getConnectionFactory(), connectionFactoryMock);
    }

    @Test
    public void testJmsListenerContainerFactory() {
        JmsListenerContainerFactory jmsListenerContainerFactory =
                messagingConfig.topicJmsListenerContainerFactory(connectionFactoryMock);

        assertThat(jmsListenerContainerFactory).isNotNull();
    }
}
*/
