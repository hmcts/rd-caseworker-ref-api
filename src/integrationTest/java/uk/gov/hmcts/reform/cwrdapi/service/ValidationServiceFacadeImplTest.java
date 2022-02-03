package uk.gov.hmcts.reform.cwrdapi.service;

import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.CaseWorkerRefApiApplication;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.config.RepositoryConfig;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfig;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.serenity5.SerenityTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PARTIAL_SUCCESS;

@SerenityTest
@WithTags({@WithTag("testType:Integration")})
@DataJpaTest
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@ContextConfiguration(classes = {CaseWorkerRefApiApplication.class, TestConfig.class, RepositoryConfig.class})
public class ValidationServiceFacadeImplTest {

    @Spy
    @Autowired
    IValidationService iValidationService;

    @InjectMocks
    ValidationServiceFacadeImpl validationServiceFacadeImpl;

    @Autowired
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;

    @MockBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Mock
    CaseWorkerAudit caseWorkerAudit;

    CaseWorkerAudit caseWorkerAuditSpy;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuditJsr() {

        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setRowId(1);
        profile.setOfficialEmail("test@abc.com");
        caseWorkerProfiles.add(profile);

        long jobId = iValidationService.updateCaseWorkerAuditStatus(AuditStatus.IN_PROGRESS, "test");
        caseWorkerAudit = CaseWorkerAudit.builder()
            .fileName("test.xlsx")
            .jobStartTime(LocalDateTime.now())
            .jobId(jobId)
            .status(PARTIAL_SUCCESS).build();

        iValidationService.getInvalidRecords(caseWorkerProfiles);

        iValidationService.updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "");
        IValidationService validationServiceFacadeImplSpy = spy(iValidationService);
        validationServiceFacadeImplSpy.saveJsrExceptionsForCaseworkerJob(jobId);
        List<ExceptionCaseWorker> exceptionCaseWorkers = exceptionCaseWorkerRepository.findByJobId(jobId);
        assertNotNull(exceptionCaseWorkers);
        String error = exceptionCaseWorkers.stream()
            .filter(s -> s.getFieldInError().equalsIgnoreCase("firstName"))
            .map(field -> field.getErrorDescription())
            .collect(Collectors.toList()).get(0);
        assertEquals(CaseWorkerConstants.FIRST_NAME_MISSING, error);
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

        verify(caseWorkerAudit, times(1)).getJobId();

    }

    @Test
    public void testInsertAudit() {
        assertTrue(validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.IN_PROGRESS, "test")
            > 0);
    }

    @Test
    public void testStartAuditJob() {
        assertTrue(validationServiceFacadeImpl.startCaseworkerAuditing(AuditStatus.IN_PROGRESS, "test")
            > 0);
    }
}

