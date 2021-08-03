package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.config.TrimStringFields;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersProfileCreationRequest")
public class CaseWorkersProfileCreationRequest {

    @JsonProperty("first_name")
    @JsonDeserialize(using = TrimStringFields.class)
    private String firstName;
    @JsonProperty("last_name")
    @JsonDeserialize(using = TrimStringFields.class)
    private String lastName;
    @JsonProperty("email_id")
    @JsonDeserialize(using = TrimStringFields.class)
    private String emailId;
    @JsonProperty("region_id")
    private Integer regionId;
    @JsonProperty("user_type")
    @JsonDeserialize(using = TrimStringFields.class)
    private String userType;
    @JsonProperty("region")
    @JsonDeserialize(using = TrimStringFields.class)
    private String region;
    @JsonProperty("suspended")
    private boolean suspended;
    @JsonProperty("idam_roles")
    private Set<String> idamRoles;
    @JsonProperty("roles")
    private List<CaseWorkerRoleRequest> roles;
    @JsonProperty("base_location")
    private List<CaseWorkerLocationRequest> baseLocations;
    @JsonProperty("work_area")
    private List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests;
    @JsonProperty("rowId")
    private long rowId;
    @JsonProperty("case_allocator")
    private boolean caseAllocator;
    @JsonProperty("task_supervisor")
    private boolean taskSupervisor;
}
