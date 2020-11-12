package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersProfileCreationRequest")
public class CaseWorkersProfileCreationRequest {

    private String firstName;
    private String lastName;
    private String emailId;
    private Integer regionId;
    private String userType;
    private String region;
    private boolean deleteFlag;
    private Set<String> idamRoles;
    private List<CaseWorkerRoleRequest> roles;
    private List<CaseWorkerLocationRequest> baseLocations;

    private List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests;

    @JsonCreator
    public CaseWorkersProfileCreationRequest( @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            @JsonProperty("email_id") String emailId,
            @JsonProperty("region_id") Integer regionId,
            @JsonProperty("user_type") String userType,
            @JsonProperty("region") String region,
            @JsonProperty("delete_flag") boolean deleteFlag,
            @JsonProperty("idam_roles") Set<String> idamRoles,
            @JsonProperty("roles") List<CaseWorkerRoleRequest> roles,
            @JsonProperty("base_location") List<CaseWorkerLocationRequest> baseLocations,
            @JsonProperty("work_area") List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailId = emailId;
        this.regionId = regionId;
        this.userType = userType;
        this.deleteFlag = deleteFlag;
        this.idamRoles = idamRoles;
        this.roles = roles;
        this.baseLocations = baseLocations;
        this.workerWorkAreaRequests = workerWorkAreaRequests;
    }


}
