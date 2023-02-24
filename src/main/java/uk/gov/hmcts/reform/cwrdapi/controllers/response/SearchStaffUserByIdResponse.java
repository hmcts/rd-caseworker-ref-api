package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class SearchStaffUserByIdResponse extends SearchStaffUserResponse {

    @Builder(builderMethodName = "withIdBuilder")
    public SearchStaffUserByIdResponse(String firstName, String lastName, String emailId, String caseWorkerId,
                                       List<ServiceResponse> services, String region, Integer regionId,
                                       List<Role> roles, boolean taskSupervisor, boolean caseAllocator,
                                       boolean suspended, boolean staffAdmin, List<Location> baseLocations,
                                       String userType, List<SkillResponse> skills, String idamStatus) {
        super(firstName, lastName, emailId, caseWorkerId, services, region, regionId, roles, taskSupervisor,
                caseAllocator, suspended, staffAdmin, baseLocations, userType, skills);
        this.idamStatus = idamStatus;
    }

    @JsonProperty("up_idam_status")
    private String idamStatus;

}
