package uk.gov.hmcts.reform.cwrdapi.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessagingConfig {

    @Value("${crd.publisher.azure.service.bus.host}")
    String host;

    @Value("${crd.publisher.azure.service.bus.topic}")
    String topic;

    @Value("${crd.publisher.azure.service.bus.username}")
    String sharedAccessKeyName;

    @Value("${crd.publisher.azure.service.bus.password}")
    String sharedAccessKeyValue;

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        String connectionString = "Endpoint=sb://"
                + host + ";SharedAccessKeyName=" + sharedAccessKeyName + ";SharedAccessKey=" + sharedAccessKeyValue;
        log.info(sharedAccessKeyValue.substring(0, 5) + " "
                + sharedAccessKeyValue.length() + " "
                + sharedAccessKeyValue.substring(sharedAccessKeyValue.length() - 6));
        log.info(host.substring(host.length() - 5));
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(new AmqpRetryOptions())
                .sender()
                .topicName(topic)
                .buildClient();
    }


}
