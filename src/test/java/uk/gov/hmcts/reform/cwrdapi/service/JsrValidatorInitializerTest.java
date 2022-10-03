package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.util.EmailDomainPropertyInitiator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

class JsrValidatorInitializerTest {

    @Spy
    @InjectMocks
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @BeforeEach
    void init() {
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,DWP.GOV.UK,hmrc.gov.uk";
        openMocks(this);
        jsrValidatorInitializer.initializeFactory();
    }

    @Test
    void testGetNoInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(0, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    void testGetInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setOfficialEmail("abc.com");
        caseWorkerProfiles.add(profile);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(1, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);

        Set<ConstraintViolation<CaseWorkerDomain>> constraintViolationSet =
                jsrValidatorInitializer.getConstraintViolations();

        assertThat(constraintViolationSet).isNotEmpty();
    }

    @Test
    void testGetInvalidJsrRecords_withDifferentEmails() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerDomain profile1 = buildCaseWorkerProfileData("tEst123-CRD3@JUSTICE.GOV.UK");
        CaseWorkerDomain profile2 = buildCaseWorkerProfileData("$%^&@justice.gov.uk");
        CaseWorkerDomain profile3 = buildCaseWorkerProfileData("user name@justice.gov.uk");
        caseWorkerProfiles.add(profile1);
        caseWorkerProfiles.add(profile2);
        caseWorkerProfiles.add(profile3);

        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(2, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

}
