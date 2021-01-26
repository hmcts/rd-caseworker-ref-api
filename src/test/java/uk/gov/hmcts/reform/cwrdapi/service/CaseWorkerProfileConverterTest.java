package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.TestSupport;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class CaseWorkerProfileConverterTest {

    @Test
    public void shouldConvertCwProfileToRequest() {
        CaseWorkerProfileConverter caseWorkerProfileConverter = new CaseWorkerProfileConverter();
        List<CaseWorkerDomain> caseWorkerDomains = TestSupport.buildCaseWorkerProfileData();
        List<CaseWorkersProfileCreationRequest> convert =
                caseWorkerProfileConverter.convert(caseWorkerDomains);
        assertNotNull(convert);
        CaseWorkersProfileCreationRequest caseWorkersProfileCreationRequest = convert.get(0);
        assertNotNull(caseWorkersProfileCreationRequest.getBaseLocations());
        assertNotNull(caseWorkersProfileCreationRequest.getIdamRoles());
        assertEquals("test@justice.gov.uk", caseWorkersProfileCreationRequest.getEmailId());
        assertEquals("test", caseWorkersProfileCreationRequest.getFirstName());
        assertEquals("test", caseWorkersProfileCreationRequest.getLastName());
        assertEquals("test", caseWorkersProfileCreationRequest.getRegion());
        assertEquals("testUser", caseWorkersProfileCreationRequest.getUserType());
        assertEquals("1", caseWorkersProfileCreationRequest
                .getBaseLocations().get(0).getLocation());
        assertTrue(caseWorkersProfileCreationRequest
                .getBaseLocations().get(0).isPrimaryFlag());
        assertEquals("area1", caseWorkersProfileCreationRequest
                .getWorkerWorkAreaRequests().get(0).getAreaOfWork());
        assertEquals("AAA1", caseWorkersProfileCreationRequest
                .getWorkerWorkAreaRequests().get(0).getServiceCode());
        assertEquals("rl1", caseWorkersProfileCreationRequest
                .getRoles().get(0).getRole());
        assertTrue(caseWorkersProfileCreationRequest
                .getRoles().get(0).isPrimaryFlag());

    }
}