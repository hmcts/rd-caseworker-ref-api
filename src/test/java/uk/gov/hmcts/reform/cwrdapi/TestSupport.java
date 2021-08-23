package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;

import java.util.List;

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
}
