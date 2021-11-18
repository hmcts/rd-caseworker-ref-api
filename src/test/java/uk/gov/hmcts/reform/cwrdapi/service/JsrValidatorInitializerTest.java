package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

public class JsrValidatorInitializerTest {

    @Spy
    @InjectMocks
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @BeforeEach
    public void init() {
        openMocks(this);
        jsrValidatorInitializer.initializeFactory();
    }

    @Test
    public void testGetNoInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(0, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    public void testGetInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setOfficialEmail("abc.com");
        caseWorkerProfiles.add(profile);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(1, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    public void testGetInvalidJsrRecords_withDifferentEmails() {
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
