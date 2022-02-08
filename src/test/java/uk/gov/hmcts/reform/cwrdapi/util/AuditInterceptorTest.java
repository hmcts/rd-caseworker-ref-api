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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;

@ExtendWith(MockitoExtension.class)
class AuditInterceptorTest {

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
    void testPreHandleWithNobody() {
        when(request.getFile(FILE)).thenReturn(null);
        Object object = new Object();
        ExcelValidationException exception = Assertions.assertThrows(ExcelValidationException.class, () ->
                interceptor.preHandle(request, response,object));

        Assertions.assertTrue(exception.getLocalizedMessage().contains(
                "There is no data in the file uploaded. Upload a valid file in xlsx or xls format"));
    }

    @Test
    void testPreHandleWithBody() {
        when(request.getFile(FILE)).thenReturn(multipartFile);
        UserInfo userInfo = UserInfo.builder().uid("122323").build();
        when(converter.getUserInfo()).thenReturn(userInfo);
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}