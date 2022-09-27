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
        CaseWorkerRoleRequest caseWorkerRoleRequest2 =
                new CaseWorkerRoleRequest("adminRole", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();
        CaseWorkerLocationRequest caseWorkerLocationRequest2 = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("LocationSecond")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest2 = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA5")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId("skill")
                .description("training")
                .build();

        SkillsRequest skillsRequest2 = SkillsRequest
                .skillsRequest()
                .skillId("skill2")
                .description("training2")
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
                .services(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest2))
                .baseLocations(List.of(caseWorkerLocationRequest, caseWorkerLocationRequest2))
                .roles(List.of(caseWorkerRoleRequest,caseWorkerRoleRequest2))
                .skills(List.of(skillsRequest,skillsRequest2))
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

    @Test
    void testIsLocation() {
        boolean response = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidLocations(staffProfileCreationRequest, context)).thenReturn(true);

        assertThat(response).isFalse();

        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest().build();
        boolean responseLocationEmpty = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidLocations(staffProfileCreationRequest, context)).thenReturn(true);

        assertThat(responseLocationEmpty).isFalse();
    }

    @Test
    void testisValidRoles() {
        boolean response = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidRoles(staffProfileCreationRequest, context)).thenReturn(false);
        assertThat(response).isFalse();

        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest().build();
        boolean responseRolesEmpty = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidRoles(staffProfileCreationRequest, context)).thenReturn(false);
        assertThat(responseRolesEmpty).isFalse();
    }

    @Test
    void isValidAreaOfWk() {

        boolean response = sut.isValid(staffProfileCreationRequest, context);
        when(sut.isValidAreaOfWk(staffProfileCreationRequest, context)).thenReturn(true);
        assertThat(response).isFalse();

        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest().build();
        when(sut.isValidRoles(staffProfileCreationRequest, context)).thenReturn(true);
        when(sut.isValidLocations(staffProfileCreationRequest, context)).thenReturn(true);
        boolean responseServiceEmpty = sut.isValid(staffProfileCreationRequest, context);

        assertThat(responseServiceEmpty).isFalse();
    }
}
