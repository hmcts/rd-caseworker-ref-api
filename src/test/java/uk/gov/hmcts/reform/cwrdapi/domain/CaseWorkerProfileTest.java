package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class CaseWorkerProfileTest {
    @Test
    public void testCaseWorkerProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();

        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWtest@test.com");
        caseWorkerProfile.setUserTypeId(1L);
        caseWorkerProfile.setRegion("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setDeleteFlag(true);
        caseWorkerProfile.setDeleteDate(LocalDateTime.now());
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerLocationId(1L);
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setLocation("TestLocation");
        caseWorkerLocation.setLocationId(13);
        caseWorkerLocation.setPrimary(false);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimary(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());

        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdate(LocalDateTime.now());

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());


        caseWorkerProfile.setCaseWorkerLocations(Collections.singletonList(caseWorkerLocation));
        caseWorkerProfile.setCaseWorkerRoles(Collections.singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerWorkAreas(Collections.singletonList(caseWorkerWorkArea));
        caseWorkerProfile.setUserType(userType);

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getEmailId(), is("CWtest@test.com"));
        assertThat(caseWorkerProfile.getUserTypeId(), is(1L));
        assertThat(caseWorkerProfile.getRegion(), is("Region"));
        assertThat(caseWorkerProfile.getRegionId(), is(12));
        assertThat(caseWorkerProfile.getDeleteFlag(), is(true));
        assertNotNull(caseWorkerProfile.getDeleteDate());
        assertNotNull(caseWorkerProfile.getCreatedDate());
        assertNotNull(caseWorkerProfile.getLastUpdate());

        assertNotNull(caseWorkerProfile.getCaseWorkerLocations());
        assertFalse(caseWorkerProfile.getCaseWorkerLocations().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getCaseWorkerLocationId(), is (1L));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocation(), is("TestLocation"));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocationId(), is(13));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getPrimary(), is(false));

        assertNotNull(caseWorkerProfile.getCaseWorkerRoles());
        assertFalse(caseWorkerProfile.getCaseWorkerRoles().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getCaseWorkerRoleId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getRoleId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getPrimary(), is(false));

        assertNotNull(caseWorkerProfile.getCaseWorkerWorkAreas());
        assertFalse(caseWorkerProfile.getCaseWorkerWorkAreas().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getCaseWorkerWorkAreaId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getAreaOfWork(), is("TestArea"));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getServiceCode(), is("SvcCode1"));

        assertNotNull(caseWorkerProfile.getUserType());
        assertThat(caseWorkerProfile.getUserType().getUserTypeId(), is(1L));
        assertThat(caseWorkerProfile.getUserType().getDescription(), is("Test Description"));

    }
}
