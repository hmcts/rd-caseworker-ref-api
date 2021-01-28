package uk.gov.hmcts.reform.cwrdapi.service;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.CaseWorkerRefApiApplication;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.config.RepositoryConfig;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfig;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PARTIAL_SUCCESS;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@DataJpaTest
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@ContextConfiguration(classes = {CaseWorkerRefApiApplication.class, TestConfig.class, RepositoryConfig.class})
public class ValidationServiceFacadeImplTest {

    @Spy
    @Autowired
    ValidationServiceFacadeImpl validationServiceFacadeImpl;

    @Autowired
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Autowired
    SimpleJpaRepository<ExceptionCaseWorker, Long> simpleJpaRepositoryException;


    @MockBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Autowired
    private IAuditAndExceptionRepositoryService auditAndExceptionRepositoryService;

    @Test
    public void testAuditJsr() {

        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setRowId(1);
        profile.setOfficialEmail("test@abc.com");
        caseWorkerProfiles.add(profile);

        long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.IN_PROGRESS, "test");
        CaseWorkerAudit caseWorkerAudit = CaseWorkerAudit.builder()
            .fileName("test.xlsx")
            .jobStartTime(LocalDateTime.now())
            .jobId(jobId)
            .status(PARTIAL_SUCCESS).build();

        validationServiceFacadeImpl.getInvalidRecords(caseWorkerProfiles);

        auditAndExceptionRepositoryService.auditSchedulerStatus(caseWorkerAudit);
        ValidationServiceFacadeImpl validationServiceFacadeImplSpy = spy(validationServiceFacadeImpl);
        validationServiceFacadeImplSpy.auditJsr(jobId);
        List<ExceptionCaseWorker> exceptionCaseWorkers = auditAndExceptionRepositoryService.getAllExceptions(jobId);
        assertNotNull(exceptionCaseWorkers);
        String error = exceptionCaseWorkers.stream()
            .filter(s -> s.getFieldInError().equalsIgnoreCase("firstName"))
            .map(field -> field.getErrorDescription())
            .collect(Collectors.toList()).get(0);
        assertEquals("must not be empty", error);
        verify(validationServiceFacadeImplSpy).auditJsr(jobId);
        assertNotNull(validationServiceFacadeImplSpy.getJsrExceptionCaseWorkers());
    }

    @Test
    public void testInsertAudit() {
        assertNotNull(validationServiceFacadeImpl.insertAudit(AuditStatus.IN_PROGRESS, "test"));
    }
}
