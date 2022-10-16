package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;

import java.util.List;

import static java.util.Collections.singletonList;

public class TestSupport {

    private TestSupport() {
        super();
    }

    public static List<CaseWorkerDomain> buildCaseWorkerProfileData() {

        return ImmutableList.of(buildCaseWorkerProfileData("test@justice.gov.uk"));
    }

    public static CaseWorkerDomain buildCaseWorkerProfileData(String email) {

        ImmutableList<Location> locations = ImmutableList.of(
                Location.builder().baseLocationId(1)
                        .locationName("1").isPrimary(true).build());

        ImmutableList<WorkArea> workAreas = ImmutableList.of(
                WorkArea.builder().serviceCode("AAA1").areaOfWork("area1").build());


        ImmutableList<Role> roles = ImmutableList.of(
                Role.builder().isPrimary(true)
                        .roleName("rl1").build(),
                Role.builder().isPrimary(true)
                        .build());

        return CaseWorkerProfile.builder()
                .firstName("test").lastName("test")
                .officialEmail(email)
                .regionId(1)
                .regionName("test")
                .userType("testUser")
                .workAreas(workAreas)
                .locations(locations)
                .roles(roles)
                .idamRoles("role1, role2")
                .suspended("N")
                .build();
    }

    public static List<CaseWorkerDomain> buildSuspendedCaseWorkerProfileData() {

        ImmutableList<Location> locations = ImmutableList.of(
                Location.builder().baseLocationId(1)
                        .locationName("1").isPrimary(true).build());

        ImmutableList<WorkArea> workAreas = ImmutableList.of(
                WorkArea.builder().serviceCode("AAA1").areaOfWork("area1").build());


        ImmutableList<Role> roles = ImmutableList.of(
                Role.builder().isPrimary(true)
                        .roleName("rl1").build(),
                Role.builder().isPrimary(true)
                        .roleName("").build());

        return ImmutableList.of(CaseWorkerProfile.builder()
                .firstName("test").lastName("test")
                .officialEmail("test@justice.gov.uk")
                .regionId(1)
                .suspended("Y")
                .regionName("test")
                .userType("testUser")
                .workAreas(workAreas)
                .locations(locations)
                .roles(roles)
                .build());
    }

    public static StaffProfileCreationRequest  buildStaffProfileRequest() {

        StaffProfileRoleRequest caseWorkerRoleRequest =
                new StaffProfileRoleRequest(1,"testRole1", true);

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
                .skillId(1)
                .description("training")
                .build();


        return StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@justice.gov.uk")
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
    }

}
