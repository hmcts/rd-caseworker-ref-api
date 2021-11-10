package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseWorkerProfileTest {

    @Test
    public void testCaseWorkerProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setId("CWID1");
        caseWorkerProfile.setUserId(1L);
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setOfficialEmail("CWtest@test.com");
        caseWorkerProfile.setUserType("User Type");
        caseWorkerProfile.setRegionName("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setSuspended("true");
        caseWorkerProfile.setCreatedTime(LocalDateTime.now());
        caseWorkerProfile.setLastUpdatedTime(LocalDateTime.now());

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getId(), is("CWID1"));
        assertThat(caseWorkerProfile.getUserId(), is(1L));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getOfficialEmail(), is("CWtest@test.com"));
        assertThat(caseWorkerProfile.getUserType(), is("User Type"));
        assertThat(caseWorkerProfile.getRegionName(), is("Region"));
        assertThat(caseWorkerProfile.getRegionId(), is(12));
        assertThat(caseWorkerProfile.getSuspended(), is("true"));
        assertNotNull(caseWorkerProfile.getCreatedTime());
        assertNotNull(caseWorkerProfile.getLastUpdatedTime());
    }

    @Test
    public void testCaseWorkerProfileBuilder() {
        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        CaseWorkerProfile caseWorkerProfile = CaseWorkerProfile.builder()
                .id("CWID1")
                .userId(1L)
                .firstName("CWFirstName")
                .lastName("CWLastName")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .idamRoles("idamRole")
                .roles(asList(caseWorkerRole))
                .locations(asList(new Location()))
                .officialEmail("caseworker@email.com")
                .regionId(1)
                .regionName("regionName")
                .suspended("true")
                .workAreas(asList(new WorkArea()))
                .userType("userType")
                .build();

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getId(), is("CWID1"));
        assertThat(caseWorkerProfile.getUserId(), is(1L));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getOfficialEmail(), is("caseworker@email.com"));
        assertThat(caseWorkerProfile.getUserType(), is("userType"));
        assertThat(caseWorkerProfile.getRegionName(), is("regionName"));
        assertThat(caseWorkerProfile.getRegionId(), is(1));
        assertThat(caseWorkerProfile.getSuspended(), is("true"));
        assertNotNull(caseWorkerProfile.getCreatedTime());
        assertNotNull(caseWorkerProfile.getLastUpdatedTime());
        assertNotNull(caseWorkerProfile.getWorkAreas());
        assertNotNull(caseWorkerProfile.getRoles());
        assertNotNull(caseWorkerProfile.getLocations());

        String caseWorkerProfileString = CaseWorkerProfile.builder()
                .id("CWID1").toString();

        assertTrue(caseWorkerProfileString.contains("CaseWorkerProfile.CaseWorkerProfileBuilder(id=CWID1"));
    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerWorkAreas() {
        WorkArea caseWorkerWorkArea = new WorkArea();
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedTime(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdatedTime(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setWorkAreas(Collections.singletonList(caseWorkerWorkArea));

        assertNotNull(caseWorkerProfile.getWorkAreas());
        assertFalse(caseWorkerProfile.getWorkAreas().isEmpty());
        assertThat(caseWorkerProfile.getWorkAreas().get(0).getAreaOfWork(), is("TestArea"));
        assertThat(caseWorkerProfile.getWorkAreas().get(0).getServiceCode(), is("SvcCode1"));
    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerRoles() {
        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setRoles(Collections.singletonList(caseWorkerRole));

        assertNotNull(caseWorkerProfile.getRoles());
        assertFalse(caseWorkerProfile.getRoles().isEmpty());
        assertThat(caseWorkerProfile.getRoles().get(0).getRoleId(), is("id"));
        assertThat(caseWorkerProfile.getRoles().get(0).getRoleName(), is("role name"));
    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerLocations() {
        Location caseWorkerLocation = new Location();
        caseWorkerLocation.setBaseLocationId(1);
        caseWorkerLocation.setLocationName("TestLocation");
        caseWorkerLocation.setPrimary(false);
        caseWorkerLocation.setCreatedTime(LocalDateTime.now());
        caseWorkerLocation.setLastUpdatedTime(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setLocations(Collections.singletonList(caseWorkerLocation));

        assertNotNull(caseWorkerProfile.getLocations());
        assertFalse(caseWorkerProfile.getLocations().isEmpty());
        assertThat(caseWorkerProfile.getLocations().get(0).getBaseLocationId(), is(1));
        assertThat(caseWorkerProfile.getLocations().get(0).getLocationName(), is("TestLocation"));
    }

    @Test
    public void testCaseWorkerProfileContainingSuspended() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setSuspended("false");
        assertThat(caseWorkerProfile.getSuspended(), is("false"));
    }


}
