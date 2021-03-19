package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    static JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Before
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

    @ParameterizedTest
    @CsvSource({"tEst123-CRD3@JUSTICE.GOV.UK,0", "$%^&@justice.gov.uk,1", "user name@justice.gov.uk,1"})
    public void testGetInvalidJsrRecords_withDifferentEmails(String email, int expectedInvalidRecords) {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        CaseWorkerProfile record = (CaseWorkerProfile) caseWorkerProfiles.get(0);
        record.setOfficialEmail(email);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(expectedInvalidRecords, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

}
