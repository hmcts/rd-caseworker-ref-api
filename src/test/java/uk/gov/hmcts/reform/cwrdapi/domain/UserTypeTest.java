package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class UserTypeTest {

    @Test
    public void testUserType() {
        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());

        assertNotNull(userType);
        assertThat(userType.getUserTypeId(), is(1L));
        assertThat(userType.getDescription(), is("Test Description"));
        assertNotNull(userType.getCreatedDate());
        assertNotNull(userType.getLastUpdate());

    }

    @Test
    public void testUserTypeContainingCwProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");

        UserType userType = new UserType();
        userType.setCaseWorkerProfiles(Collections.singletonList(caseWorkerProfile));

        assertFalse(userType.getCaseWorkerProfiles().isEmpty());
    }
}
