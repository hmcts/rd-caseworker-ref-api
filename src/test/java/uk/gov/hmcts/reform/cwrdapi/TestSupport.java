package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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


    public static boolean validateSearchUserProfileResponse(ResponseEntity<List<SearchStaffUserResponse>> response,
                                                            SearchRequest searchReq) {

        List<SearchStaffUserResponse> validResponse;
        List<SearchStaffUserResponse> body = response.getBody();
        if (body != null) {
            validResponse = body.stream()
                    .filter(Objects::nonNull)
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getServiceCode()).isEmpty()) {
                            return true;
                        }
                        if (Optional.ofNullable(searchStaffUserResponse.getServices()).isEmpty()) {
                            return false;
                        }

                        return searchStaffUserResponse.getServices().stream()
                                .anyMatch(service -> Optional.ofNullable(service).isPresent()
                                        && searchReq.getServiceCode().toLowerCase().contains(service.getServiceCode()
                                        .toLowerCase())
                                );
                    })
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getLocation()).isEmpty()) {
                            return true;
                        }
                        if (Optional.ofNullable(searchStaffUserResponse.getBaseLocations()).isEmpty()) {
                            return false;
                        }

                        return searchStaffUserResponse.getBaseLocations().stream()

                                .anyMatch(location -> Optional.ofNullable(location).isPresent()
                                        && searchReq.getLocation().toLowerCase().contains(String.valueOf(location
                                        .getBaseLocationId()))
                                );
                    })
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getUserType()).isEmpty()) {
                            return true;
                        }
                        return Optional.ofNullable(searchStaffUserResponse.getUserType()).isPresent();
                    })
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getJobTitle()).isEmpty()) {
                            return true;
                        }
                        if (Optional.ofNullable(searchStaffUserResponse.getRoles()).isEmpty()) {
                            return false;
                        }
                        return searchStaffUserResponse.getRoles().stream()

                                .anyMatch(roles -> Optional.ofNullable(roles).isPresent()
                                        && roles.getRoleId().toLowerCase().contains(searchReq.getJobTitle()
                                        .toLowerCase())
                                );
                    })
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getSkill()).isEmpty()) {
                            return true;
                        }
                        if (Optional.ofNullable(searchStaffUserResponse.getSkills()).isEmpty()) {
                            return false;
                        }
                        return searchStaffUserResponse.getSkills().stream()

                                .anyMatch(skill -> Optional.ofNullable(skill).isPresent()
                                        && skill.getSkillId() == Integer.parseInt(searchReq.getSkill())
                                );
                    })
                    .filter(searchStaffUserResponse -> {
                        if (Optional.ofNullable(searchReq.getRole()).isEmpty()) {
                            return true;
                        }
                        boolean valid = searchReq.getRole().contains("task supervisor") && searchStaffUserResponse
                                .isTaskSupervisor();

                        if (searchReq.getRole().contains("case allocator") && searchStaffUserResponse
                                .isCaseAllocator()) {
                            valid = true;
                        }
                        if (searchReq.getRole().contains("Staff Administrator") && searchStaffUserResponse
                                .isStaffAdmin()) {
                            valid = true;
                        }


                        return valid;
                    })


                    .toList();
            Comparator<SearchStaffUserResponse> comparator
                    = Comparator.comparing(SearchStaffUserResponse::getLastName);

            List<SearchStaffUserResponse> sorted = new ArrayList<>(body);
            sorted.sort(comparator);

            if (!body.equals(sorted)) {
                return false;
            }

            return validResponse.size() == body.size();

        }

        return false;

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
