package uk.gov.hmcts.reform.cwrdapi.servicebus.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.util.ErrorHandler;

@Slf4j
public class JmsErrorHandler implements ErrorHandler {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public void handleError(@NonNull Throwable throwable) {
        log.warn("{}:: Spring JMS custom error handling example", loggingComponentName);
        log.error(throwable.getCause().getMessage());
    }
}

