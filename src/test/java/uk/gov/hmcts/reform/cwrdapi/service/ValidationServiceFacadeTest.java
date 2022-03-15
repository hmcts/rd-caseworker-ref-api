package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.AuditRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

@ExtendWith(MockitoExtension.class)
class ValidationServiceFacadeTest {

    ValidationServiceFacadeImpl validationServiceFacadeImpl = spy(new ValidationServiceFacadeImpl());

    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = mock(JwtGrantedAuthoritiesConverter.class);


    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository = mock(ExceptionCaseWorkerRepository.class);

    @SuppressWarnings("unchecked")
    AuditRepository caseWorkerAuditRepository = mock(AuditRepository.class);

    CaseWorkerAudit caseWorkerAudit = mock(CaseWorkerAudit.class);

    @BeforeEach
    void init() throws Exception {
        setField(validationServiceFacadeImpl, "exceptionCaseWorkerRepository",
            exceptionCaseWorkerRepository);
        setField(validationServiceFacadeImpl, "caseWorkerAuditRepository",
            caseWorkerAuditRepository);

        setField(validationServiceFacadeImpl, "jwtGrantedAuthoritiesConverter", jwtGrantedAuthoritiesConverter);
    }

    @Test
    void testGetInvalidRecords() throws Exception {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        setField(validationServiceFacadeImpl, "jsrValidatorInitializer", jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);

        validationServiceFacadeImpl.getInvalidRecords(caseWorkerProfiles);
        verify(validationServiceFacadeImpl).getInvalidRecords(caseWorkerProfiles);
    }

    @Test
    void testAuditJsr() {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        setField(validationServiceFacadeImpl, "jsrValidatorInitializer", jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        caseWorkerProfiles.add(profile);
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        //doNothing().when(auditAndExceptionRepositoryServiceImpl).auditException(anyList());
        validationServiceFacadeImpl.saveJsrExceptionsForCaseworkerJob(1);
        verify(validationServiceFacadeImpl).saveJsrExceptionsForCaseworkerJob(1);
    }

    @Test
    void testAuditJsrWithException() {
        CaseWorkerDomain domain = CaseWorkerProfile.builder().build();
        Assertions.assertThrows(Exception.class, () -> {
            Field field = invokeMethod(validationServiceFacadeImpl, "getKeyFiled", domain);
            Object[] objects = {field, domain};
            invokeMethod(validationServiceFacadeImpl, "getKeyFieldValue", objects);
        });
    }

    @Test
    void testInsertAudit() {
        when(caseWorkerAuditRepository.save(any())).thenReturn(CaseWorkerAudit.builder().jobId(1L).build());
        validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");

        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        long jobId = validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");
        verify(validationServiceFacadeImpl, times(2))
            .updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "CWR-Insert");
        assertEquals(1, jobId);
    }

    @Test
    void testUpdateAudit() {
        setField(validationServiceFacadeImpl, "caseWorkerAudit", CaseWorkerAudit.builder().jobId(1L).build());
        when(caseWorkerAuditRepository.save(any())).thenReturn(CaseWorkerAudit.builder().jobId(1L).build());
        long jobId = validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "CWR-Update");
        verify(validationServiceFacadeImpl, times(1))
            .updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "CWR-Update");
        assertEquals(1, jobId);
    }

    @Test
    public void testCreateException() {
        when(validationServiceFacadeImpl.createException(1L, "test", 1L))
                .thenReturn(ExceptionCaseWorker.builder().jobId(1L).build());
        assertNotNull(validationServiceFacadeImpl.createException(1L, "test", 1L));

        validationServiceFacadeImpl.createException(1L, "testFailure", 1L);
        verify(validationServiceFacadeImpl)
            .createException(1L, "testFailure", 1L);

        validationServiceFacadeImpl.createException(1L, "testFailure", 0L);
        verify(validationServiceFacadeImpl)
            .createException(1L, "testFailure", 0L);
    }

    @Test
    void testStartAuditing() {
        setField(validationServiceFacadeImpl, "caseWorkerAudit", CaseWorkerAudit.builder().build());
        when(caseWorkerAuditRepository.save(any())).thenReturn(CaseWorkerAudit.builder().jobId(1L).build());
        long jobId = validationServiceFacadeImpl.startCaseworkerAuditing(AuditStatus.PARTIAL_SUCCESS, "CWR-Start");
        verify(validationServiceFacadeImpl, times(1))
                .startCaseworkerAuditing(AuditStatus.PARTIAL_SUCCESS, "CWR-Start");
        assertEquals(1, jobId);
    }

    @Test
    public void testCaseWorkerAudit() {

        AuditStatus auditStatus = AuditStatus.PARTIAL_SUCCESS;

        when(caseWorkerAudit.getJobId()).thenReturn(1L);
        when(caseWorkerAudit.getStatus()).thenReturn("Partial Success");
        when(caseWorkerAudit.getJobEndTime()).thenCallRealMethod();
        CaseWorkerAudit caseWorkerAudit2 = CaseWorkerAudit.builder()
                .fileName("test.xlsx")
                .jobStartTime(LocalDateTime.now())
                .jobId(1L)
                .status(auditStatus.getStatus()).build();

        ReflectionTestUtils.setField(validationServiceFacadeImpl, "caseWorkerAudit", caseWorkerAudit2);
        long jobId = caseWorkerAudit.getJobId();

        CaseWorkerAudit caseWorkerAudit1 = validationServiceFacadeImpl.createOrUpdateCaseworkerAudit(auditStatus, "");
        List<ExceptionCaseWorker> exceptionCaseWorkers = exceptionCaseWorkerRepository.findByJobId(jobId);

        assertNotNull(caseWorkerAudit);
        assertEquals(caseWorkerAudit1.getStatus(), "Partial Success");
        assertEquals(caseWorkerAudit1.getJobId(), Long.valueOf(0));
        assertNotNull(caseWorkerAudit1.getJobEndTime());
        assertNotNull(caseWorkerAudit1.getStatus());

        verify(caseWorkerAudit, times(1)).getJobId();

    }

    @Test
    public void testGetAuditJobId() {
        when(validationServiceFacadeImpl.getAuditJobId()).thenReturn(1L);
        assertEquals(validationServiceFacadeImpl.getAuditJobId(), 1L);
        assertNotEquals(validationServiceFacadeImpl.getAuditJobId(), 0L);
    }

}
