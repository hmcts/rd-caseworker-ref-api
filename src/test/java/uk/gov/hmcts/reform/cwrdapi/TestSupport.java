package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CasWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;

import java.util.List;

public class TestSupport {

    private TestSupport() {
        super();
    }

    public static List<CasWorkerDomain> buildCaseWorkerProfileData() {

        ImmutableList<Location> locations = ImmutableList.of(
            Location.builder().baseLocationId(1)
                .locationName("1").isPrimary(true).build());

        ImmutableList<WorkArea> workAreas = ImmutableList.of(
            WorkArea.builder().serviceCode("AAA1").areaOfWork("area1").build());


        ImmutableList<Role> roles = ImmutableList.of(
            Role.builder().isPrimary(true)
                .roleName("rl1").build());

        return ImmutableList.of(CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@test.com")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .workAreas(workAreas)
            .locations(locations)
            .roles(roles)
            .build());
    }
}
