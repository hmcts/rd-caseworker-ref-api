package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.TestSupport;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class CaseWorkerProfileConverterTest {

    public CaseWorkerProfileConverter caseWorkerProfileConverter = new CaseWorkerProfileConverter();
    public CaseWorkerProfileConverter caseWorkerProfileConverterMock = mock(CaseWorkerProfileConverter.class);

    @Test
    public void shouldConvertCwProfileToRequest() {
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

    @Test
    public void shouldConvertSuspendedCwProfileToRequest() {
        List<CaseWorkerDomain> caseWorkerDomains = TestSupport.buildSuspendedCaseWorkerProfileData();
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
        assertTrue(caseWorkersProfileCreationRequest
                .isSuspended());
    }

    @Test
    public void testGenerateIdamRoles() {
        Set<String> roles = caseWorkerProfileConverter.generateIdamRoles("role,role1,role2");

        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertThat(roles.size()).isNotZero();

        Set<String> roles1 = caseWorkerProfileConverter.generateIdamRoles("role");

        assertNotNull(roles1);
        assertFalse(roles1.isEmpty());
        assertThat(roles1.size()).isNotZero();
    }

    @Test
    public void testIsSuspended() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setSuspended("Y");

        assertTrue(caseWorkerProfileConverter.isSuspended(caseWorkerProfile));

        CaseWorkerProfile caseWorkerProfile1 = new CaseWorkerProfile();
        caseWorkerProfile.setSuspended("N");

        assertFalse(caseWorkerProfileConverter.isSuspended(caseWorkerProfile1));
    }

    @Test
    public void testGetSuspendedRowIds() {
        List<Long> suspendedIds = new ArrayList<>();
        suspendedIds.add(1L);

        when(caseWorkerProfileConverterMock.getSuspendedRowIds()).thenReturn(suspendedIds);

        List<Long> suspendedRowIds = caseWorkerProfileConverterMock.getSuspendedRowIds();
        assertNotNull(suspendedRowIds);
        assertThat(suspendedRowIds.size()).isNotZero();
        assertThat(suspendedRowIds.get(0)).isEqualTo(1L);
    }

}