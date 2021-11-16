package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.AuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;

@ExtendWith(MockitoExtension.class)
public class AuditInterceptorTest {

    @InjectMocks
    AuditInterceptor interceptor;

    @Mock
    MultipartHttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    MultipartFile multipartFile;

    @Mock
    AuditRepository auditRepository;

    @Mock
    ValidationServiceFacadeImpl validationServiceFacadeImpl;

    @Mock
    JwtGrantedAuthoritiesConverter converter;

    @Test
    public void testPreHandleWithNobody() {
        when(request.getFile(FILE)).thenReturn(null);
        Assertions.assertThrows(ExcelValidationException.class, () -> {
            interceptor.preHandle(request, response, new Object());
            verify(interceptor, times(1)).preHandle(eq(request), eq(response), any());
        });
    }

    @Test
    public void testPreHandleWithBody() {
        when(request.getFile(FILE)).thenReturn(multipartFile);
        UserInfo userInfo = UserInfo.builder().uid("122323").build();
        when(converter.getUserInfo()).thenReturn(userInfo);
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}