package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseWorkerChildListValidatorTest {

    CaseWorkerChildListValidator sut = mock(CaseWorkerChildListValidator.class);
    CaseWorkerProfile caseWorkerProfile;
    ConstraintValidatorContext context;


    @BeforeEach
    void setUp() {
        List<WorkArea> workAreas = new ArrayList<>();
        WorkArea workArea = mock(WorkArea.class);
        workAreas.add(workArea);

        List<Role> roles = new ArrayList<>();
        Role role = mock(Role.class);
        roles.add(role);

        caseWorkerProfile = new CaseWorkerProfile();
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
        caseWorkerProfile.setCaseAllocator("caseAllocator");
        caseWorkerProfile.setTaskSupervisor("taskSupervisor");
        caseWorkerProfile.setWorkAreas(workAreas);
        caseWorkerProfile.setRoles(roles);

        context = mock(ConstraintValidatorContext.class);

        when(role.getRoleName()).thenReturn("RoleName");
    }

    @Test
    void testIsValid() {
        boolean response = sut.isValid(caseWorkerProfile, context);
        when(sut.isValidLocations(caseWorkerProfile, context)).thenReturn(true);

        assertThat(response).isFalse();
    }

}
