package uk.gov.hmcts.reform.cwrdapi.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import uk.gov.hmcts.reform.cwrdapi.servicebus.messaging.JmsErrorHandler;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;

@Configuration
@Slf4j
public class MessagingConfig {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Bean
    public String jmsUrlString(@Value("${cwrd.host}") final String host) {
        return String.format("amqps://%1s?amqp.idleTimeout=3600000", host);
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory(
            @Value("${spring.application.name}") final String clientId,
            @Value("${cwrd.username}") final String username,
            @Value("${cwrd.password}") final String password,
            @Autowired final String jmsUrlString,
            @Autowired(required = false) final SSLContext jmsSslContext,
            @Value("${cwrd.trustAllCerts}") final boolean trustAllCerts) {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(jmsUrlString);
        jmsConnectionFactory.setUsername(username);
        jmsConnectionFactory.setPassword(password);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setReceiveLocalOnly(true);
        if (trustAllCerts && jmsSslContext != null) {
            jmsConnectionFactory.setSslContext(jmsSslContext);
        }

        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        JmsTemplate returnValue = new JmsTemplate();
        returnValue.setConnectionFactory(jmsConnectionFactory);
        returnValue.setMessageConverter(new MappingJackson2MessageConverter());
        return returnValue;
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public JmsListenerContainerFactory topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        log.info("{}:: Creating JMSListenerContainer bean for topics..", loggingComponentName);
        DefaultJmsListenerContainerFactory returnValue = new DefaultJmsListenerContainerFactory();
        returnValue.setConnectionFactory(connectionFactory);
        returnValue.setSubscriptionDurable(Boolean.TRUE);
        returnValue.setErrorHandler(new JmsErrorHandler());
        return returnValue;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        return converter;
    }

}
