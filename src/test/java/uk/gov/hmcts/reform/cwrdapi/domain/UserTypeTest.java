package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTypeTest {

    @Test
    void testUserType() {
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
    void testUserTypeContainingCwProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");

        UserType userType = new UserType();
        userType.setCaseWorkerProfiles(Collections.singletonList(caseWorkerProfile));

        assertFalse(userType.getCaseWorkerProfiles().isEmpty());
    }
}
