/*
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
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
    IValidationService validationServiceFacadeImpl;

    @Autowired
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Autowired
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;

    @MockBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;


    @Test
    public void testAuditJsr() {

        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setRowId(1);
        profile.setOfficialEmail("test@abc.com");
        caseWorkerProfiles.add(profile);

        long jobId = validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.IN_PROGRESS, "test");
        CaseWorkerAudit caseWorkerAudit = CaseWorkerAudit.builder()
            .fileName("test.xlsx")
            .jobStartTime(LocalDateTime.now())
            .jobId(jobId)
            .status(PARTIAL_SUCCESS).build();

        validationServiceFacadeImpl.getInvalidRecords(caseWorkerProfiles);

        validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.PARTIAL_SUCCESS, "");
        IValidationService validationServiceFacadeImplSpy = spy(validationServiceFacadeImpl);
        validationServiceFacadeImplSpy.saveJsrExceptionsForCaseworkerJob(jobId);
        List<ExceptionCaseWorker> exceptionCaseWorkers = exceptionCaseWorkerRepository.findByJobId(jobId);
        assertNotNull(exceptionCaseWorkers);
        String error = exceptionCaseWorkers.stream()
            .filter(s -> s.getFieldInError().equalsIgnoreCase("firstName"))
            .map(field -> field.getErrorDescription())
            .collect(Collectors.toList()).get(0);
        assertEquals(CaseWorkerConstants.FIRST_NAME_MISSING, error);
        verify(validationServiceFacadeImplSpy).saveJsrExceptionsForCaseworkerJob(jobId);
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

*/
