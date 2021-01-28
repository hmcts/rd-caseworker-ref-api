package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditAndExceptionRepositoryServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

public class ValidationServiceFacadeTest {

    ValidationServiceFacadeImpl validationServiceFacadeImpl = spy(new ValidationServiceFacadeImpl());

    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = mock(JwtGrantedAuthoritiesConverter.class);


    IAuditAndExceptionRepositoryService auditAndExceptionRepositoryServiceImpl =
        spy(new AuditAndExceptionRepositoryServiceImpl());

    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository = mock(ExceptionCaseWorkerRepository.class);

    @SuppressWarnings("unchecked")
    SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository = mock(SimpleJpaRepository.class);


    @Before
    public void init() throws Exception {
        setField(auditAndExceptionRepositoryServiceImpl, "exceptionCaseWorkerRepository",
            exceptionCaseWorkerRepository);
        setField(auditAndExceptionRepositoryServiceImpl, "caseWorkerAuditRepository",
            caseWorkerAuditRepository);
        setField(validationServiceFacadeImpl, "auditAndExceptionRepositoryService",
            auditAndExceptionRepositoryServiceImpl);
        setField(validationServiceFacadeImpl, "jwtGrantedAuthoritiesConverter", jwtGrantedAuthoritiesConverter);
    }

    @Test
    public void testGetInvalidRecords() throws Exception {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        setField(validationServiceFacadeImpl, "jsrValidatorInitializer", jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);

        validationServiceFacadeImpl.getInvalidRecords(caseWorkerProfiles);
        verify(validationServiceFacadeImpl).getInvalidRecords(caseWorkerProfiles);
    }

    @Test
    public void testAuditJsr() {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        setField(validationServiceFacadeImpl, "jsrValidatorInitializer", jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        caseWorkerProfiles.add(profile);
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        doNothing().when(auditAndExceptionRepositoryServiceImpl).auditException(anyList());
        validationServiceFacadeImpl.auditJsr(1);
        verify(validationServiceFacadeImpl).auditJsr(1);
    }

    @Test(expected = Exception.class)
    public void testAuditJsrWithException() {
        CaseWorkerDomain domain = CaseWorkerProfile.builder().build();
        Field field = invokeMethod(validationServiceFacadeImpl, "getKeyFiled",domain);
        Object [] objects = {field, domain};
        invokeMethod(validationServiceFacadeImpl, "getKeyFieldValue", objects);
    }

    @Test
    public void testInsertAudit() {
        when(caseWorkerAuditRepository.save(any())).thenReturn(CaseWorkerAudit.builder().jobId(1L).build());
        validationServiceFacadeImpl.insertAudit(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");

        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");
        verify(validationServiceFacadeImpl, times(2))
            .insertAudit(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");
        assertEquals(1, jobId);
    }

    @Test
    public void testUpdateAudit() {
        setField(validationServiceFacadeImpl, "caseWorkerAudit", CaseWorkerAudit.builder().build());
        when(caseWorkerAuditRepository.save(any())).thenReturn(CaseWorkerAudit.builder().jobId(1L).build());
        long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.PARTIAL_SUCCESS, "CWR-Update");
        verify(validationServiceFacadeImpl, times(1))
            .insertAudit(AuditStatus.PARTIAL_SUCCESS, "CWR-Update");
        assertEquals(1, jobId);
    }

    @Test
    public void testCreateException() {
        validationServiceFacadeImpl.createException(1L, "testFailure", 1L);
        verify(validationServiceFacadeImpl)
            .createException(1L, "testFailure", 1L);

        validationServiceFacadeImpl.createException(1L, "testFailure", 0L);
        verify(validationServiceFacadeImpl)
            .createException(1L, "testFailure", 0L);
    }
}
