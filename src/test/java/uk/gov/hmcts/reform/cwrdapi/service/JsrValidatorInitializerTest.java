package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

class JsrValidatorInitializerTest {

    @Spy
    @InjectMocks
    static JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @BeforeEach
    public void init() {
        openMocks(this);
        jsrValidatorInitializer.initializeFactory();
    }

    @Test
    void testGetNoInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        Assertions.assertEquals(0, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    void testGetInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setOfficialEmail("abc.com");
        caseWorkerProfiles.add(profile);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        Assertions.assertEquals(1, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @ParameterizedTest
    @CsvSource({"tEst123-CRD3@JUSTICE.GOV.UK,0", "$%^&@justice.gov.uk,1", "user name@justice.gov.uk,1"})
    void testGetInvalidJsrRecords_withDifferentEmails(String email, int expectedInvalidRecords) {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        CaseWorkerProfile record = (CaseWorkerProfile) caseWorkerProfiles.get(0);
        record.setOfficialEmail(email);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        Assertions.assertEquals(expectedInvalidRecords, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

}
