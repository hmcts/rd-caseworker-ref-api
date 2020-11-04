package uk.gov.hmcts.reform.cwrdapi.servicebus.messaging;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JmsErrorHandlerTest {

    JmsErrorHandler jmsErrorHandler = new JmsErrorHandler();
    Throwable throwableMock = mock(Throwable.class);

    @Test(expected = Test.None.class)
    public void testHandleError() {
        when(throwableMock.getCause()).thenReturn(throwableMock);
        when(throwableMock.getCause().getMessage()).thenReturn("message");
        jmsErrorHandler.handleError(throwableMock);
    }
}
