package uk.gov.hmcts.reform.cwrdapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PARTIAL_SUCCESS;

@SerenityTest
@WithTags({@WithTag("testType:Integration")})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@ContextConfiguration(classes = {CaseWorkerRefApiApplication.class, TestConfig.class, RepositoryConfig.class})
class ValidationServiceFacadeImplTest {

    @Spy
    @Autowired
    IValidationService validationServiceFacadeImpl;

    @Autowired
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;

    @MockBean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    StaffAuditRepository staffAuditRepository = mock(StaffAuditRepository.class);
    ObjectMapper objectMapper = mock(ObjectMapper.class);

    @Test
    void testAuditJsr() {
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
            .toList().get(0);
        assertEquals(CaseWorkerConstants.FIRST_NAME_MISSING, error);
    }

    @Test
    void testInsertAudit() {
        assertTrue(validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.IN_PROGRESS, "test")
            > 0);
    }

    @Test
    void testStartAuditJob() {
        assertTrue(validationServiceFacadeImpl.startCaseworkerAuditing(AuditStatus.IN_PROGRESS, "test")
            > 0);
    }

}

