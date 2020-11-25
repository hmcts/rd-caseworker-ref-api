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
@Builder
public class CaseWorkersProfileCreationRequest {

    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String emailId;
    @JsonProperty("region_id")
    private Integer regionId;
    @JsonProperty("user_type")
    private String userType;
    @JsonProperty("region")
    private String region;
    @JsonProperty("delete_flag")
    private boolean deleteFlag;
    @JsonProperty("idam_roles")
    private Set<String> idamRoles;
    @JsonProperty("roles")
    private List<CaseWorkerRoleRequest> roles;
    @JsonProperty("base_location")
    private List<CaseWorkerLocationRequest> baseLocations;

    private List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests;

    @JsonCreator
    public CaseWorkersProfileCreationRequest(@JsonProperty("first_name") String firstName,
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
