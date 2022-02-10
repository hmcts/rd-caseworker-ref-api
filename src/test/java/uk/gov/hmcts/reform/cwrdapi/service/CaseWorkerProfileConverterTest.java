package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.TestSupport;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

class CaseWorkerProfileConverterTest {

    CaseWorkerProfileConverter caseWorkerProfileConverter = new CaseWorkerProfileConverter();
    CaseWorkerProfileConverter caseWorkerProfileConverterMock = mock(CaseWorkerProfileConverter.class);

    @Test
    void shouldConvertCwProfileToRequest() {
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
    void shouldConvertSuspendedCwProfileToRequest() {
        List<CaseWorkerDomain> caseWorkerDomains = TestSupport.buildSuspendedCaseWorkerProfileData();
        List<CaseWorkersProfileCreationRequest> convert =
                caseWorkerProfileConverter.convert(caseWorkerDomains);
        caseWorkerDomains.get(0).setRowId(10);
        assertNotEquals(0,caseWorkerDomains.get(0).getRowId());
        assertNotNull(convert);
        CaseWorkersProfileCreationRequest caseWorkersProfileCreationRequest = convert.get(0);
        assertNotNull(caseWorkersProfileCreationRequest.getBaseLocations());
        assertNull(caseWorkersProfileCreationRequest.getIdamRoles());
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

        List<Long> suspendedRowIds = caseWorkerProfileConverter.getSuspendedRowIds();
        assertNotNull(suspendedRowIds);
        assertThat(suspendedRowIds).isNotEmpty();
        assertThat(suspendedRowIds.size()).isNotZero();
        assertThat(suspendedRowIds.get(0)).isZero();
    }

    @Test
    void testGetSuspendedRowIds() {
        List<Long> suspendedIds = new ArrayList<>();
        suspendedIds.add(1L);

        when(caseWorkerProfileConverterMock.getSuspendedRowIds()).thenReturn(suspendedIds);

        List<Long> suspendedRowIds = caseWorkerProfileConverterMock.getSuspendedRowIds();
        assertNotNull(suspendedRowIds);
        assertThat(suspendedRowIds).isNotEmpty();
        assertThat(suspendedRowIds.size()).isNotZero();
        assertThat(suspendedRowIds.get(0)).isEqualTo(1L);
    }

    @Test
    void testIsNotSuspended() {
        CaseWorkerProfile caseWorkerProfile = CaseWorkerProfile.builder()
                .firstName("test").lastName("test")
                .officialEmail("email@gov.justice.uk")
                .regionId(1)
                .regionName("test")
                .userType("testUser")
                .idamRoles("role1, role2")
                .suspended("N")
                .build();

        assertFalse(caseWorkerProfileConverter.isSuspended(caseWorkerProfile));
    }

    @Test
    void testIsSuspended() {
        CaseWorkerProfile caseWorkerProfile = CaseWorkerProfile.builder()
                .firstName("test").lastName("test")
                .officialEmail("email@gov.justice.uk")
                .regionId(1)
                .regionName("test")
                .userType("testUser")
                .idamRoles("role1, role2")
                .suspended("Y")
                .build();

        assertTrue(caseWorkerProfileConverter.isSuspended(caseWorkerProfile));
    }

}