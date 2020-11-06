package uk.gov.hmcts.reform.cwrdapi.domain;

import org.hibernate.usertype.UserType;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerProfileTest {
    @Test
    public void testCaseWorkerProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();

        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWtest@test.com");
        caseWorkerProfile.setUserTypeId(1);
        caseWorkerProfile.setRegion("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setDeleteFlag(true);
        caseWorkerProfile.setDeleteDate(LocalDate.now());
        caseWorkerProfile.setCreatedDate(LocalDate.now());
        caseWorkerProfile.setLastUpdate(LocalDate.now());

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerLocationId(1L);
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setLocation("TestLocation");
        caseWorkerLocation.setLocationId(13);
        caseWorkerLocation.setPrimaryFlag(false);
        caseWorkerLocation.setCreatedDate(LocalDate.now());
        caseWorkerLocation.setLastUpdate(LocalDate.now());

        CaseWorkerRoleTest caseWorkerRole = new CaseWorkerRoleTest();
        caseWorkerRole.setCaseWorkerRoleId(1);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId("Role1");
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDate.now());
        caseWorkerRole.setLastUpdate(LocalDate.now());

        CaseWorkerWorkAreaTest caseWorkerWorkArea = new CaseWorkerWorkAreaTest();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDate.now());
        caseWorkerWorkArea.setLastUpdate(LocalDate.now());

        UserType userType = new UserType();
        userType.setUserTypeId(1);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDate.now());
        userType.setLastUpdate(LocalDate.now());


        caseWorkerProfile.setCaseWorkerLocation(Collections.singletonList(caseWorkerLocation));
        caseWorkerProfile.setCaseWorkerRole(Collections.singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerWorkArea(Collections.singletonList(caseWorkerWorkArea));
        caseWorkerProfile.setUserType(userType);

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerProfile.getFirstName(), "CWFirstName");
        assertThat(caseWorkerProfile.getLastName(), "CWLastName");
        assertThat(caseWorkerProfile.getEmailId(), "CWtest@test.com");
        assertThat(caseWorkerProfile.getUserTypeId(), "CWID1");
        assertThat(caseWorkerProfile.getRegion(), "Region");
        assertThat(caseWorkerProfile.getRegionId(), 12);
        assertThat(caseWorkerProfile.getDeleteFlag(), true);
        assertThat(caseWorkerProfile.getDeleteDate(), LocalDate.now());
        assertThat(caseWorkerProfile.getCreatedDate(), LocalDate.now());
        assertThat(caseWorkerProfile.getLastUpdate(), LocalDate.now());

        assertNotNull(caseWorkerProfile.getCaseWorkerLocation());
        assertThat(caseWorkerProfile.getCaseWorkerLocation().getCaseWorkerLocationId(), 1L);
        assertThat(caseWorkerProfile.getCaseWorkerLocation().getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerProfile.getCaseWorkerLocation().getLocation(), "TestLocation");
        assertThat(caseWorkerProfile.getCaseWorkerLocation().getLocationId(), 13);
        assertThat(caseWorkerProfile.getCaseWorkerLocation().getPrimaryFlag(), false);

        assertNotNull(caseWorkerProfile.getCaseWorkerRole());
        assertThat(caseWorkerProfile.getCaseWorkerRole().getCaseWorkerRoleId(), 1L);
        assertThat(caseWorkerProfile.getCaseWorkerRole().getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerProfile.getCaseWorkerRole().getRoleId(), "Role1");
        assertThat(caseWorkerProfile.getCaseWorkerRole().getPrimaryFlag(), false);

        assertNotNull(caseWorkerProfile.getCaseWorkerWorkArea());
        assserThat(caseWorkerProfile.getCaseWorkerWorkArea().getCaseWorkerWorkAreaId(), 1L);
        assserThat(caseWorkerProfile.getCaseWorkerWorkArea().getCaseWorkerId(), "CWID1");
        assserThat(caseWorkerProfile.getCaseWorkerWorkArea().getAreaOfWork(), "TestArea");
        assserThat(caseWorkerProfile.getCaseWorkerWorkArea().getServiceCode(), "SvcCode1");

        assertNotNull(caseWorkerProfile.getUserType());
        assertThat(caseWorkerProfile.getUserType().getUserTypeId(), 1);
        assertThat(caseWorkerProfile.getUserType().getDescription(), "Test Description");

    }
}
