package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaffProfileChildListValidatorTest {

    StaffProfileChildListValidator sut = mock(StaffProfileChildListValidator.class);
    StaffProfileCreationRequest staffProfileCreationRequest;
    ConstraintValidatorContext context;


    @BeforeEach
    void setUp() {
        List<WorkArea> workAreas = new ArrayList<>();
        WorkArea workArea = mock(WorkArea.class);
        workAreas.add(workArea);

        List<Role> roles = new ArrayList<>();
        Role role = mock(Role.class);
        roles.add(role);

        CaseWorkerRoleRequest caseWorkerRoleRequest =
                new CaseWorkerRoleRequest("testRole", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId("skill")
                .description("training")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .roles(singletonList(caseWorkerRoleRequest))
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(caseWorkerRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();

        context = mock(ConstraintValidatorContext.class);

        when(role.getRoleName()).thenReturn("RoleName");
    }

    @Test
    void testIsValid() {
        boolean response = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidLocations(staffProfileCreationRequest, context)).thenReturn(true);

        assertThat(response).isFalse();
    }

}
