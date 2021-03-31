package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;

@RunWith(MockitoJUnitRunner.class)
public class AuditInterceptorTest {

    @InjectMocks
    AuditInterceptor interceptor;

    @Mock
    MultipartHttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Test(expected = ExcelValidationException.class)
    public void testPreHandleWithNobody() {
        when(request.getFile(FILE)).thenReturn(null);
        interceptor.preHandle(request, response, new Object());
        verify(interceptor, times(1)).preHandle(eq(request), eq(response), any());
    }
}
