package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonDeserialize(using = TrimStringFields.class)
    private String firstName;
    @JsonDeserialize(using = TrimStringFields.class)
    private String lastName;
    @JsonDeserialize(using = TrimStringFields.class)
    private String emailId;
    private Integer regionId;
    @JsonDeserialize(using = TrimStringFields.class)
    private String userType;
    @JsonDeserialize(using = TrimStringFields.class)
    private String region;
    private boolean suspended;
    private Set<String> idamRoles;
    private List<CaseWorkerRoleRequest> roles;
    private List<CaseWorkerLocationRequest> baseLocations;

    private List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests;
    private long rowId;
    private boolean caseAllocator;
    private boolean taskSupervisor;

    @JsonCreator
    public CaseWorkersProfileCreationRequest(@JsonProperty("first_name") String firstName,
                                             @JsonProperty("last_name") String lastName,
                                             @JsonProperty("email_id") String emailId,
                                             @JsonProperty("region_id") Integer regionId,
                                             @JsonProperty("user_type") String userType,
                                             @JsonProperty("region") String region,
                                             @JsonProperty("suspended") boolean suspended,
                                             @JsonProperty("idam_roles") Set<String> idamRoles,
                                             @JsonProperty("roles") List<CaseWorkerRoleRequest> roles,
                                             @JsonProperty("base_location") List<CaseWorkerLocationRequest> baseLocations,
                                             @JsonProperty("work_area") List<CaseWorkerWorkAreaRequest> workerWorkAreaRequests,
                                             @JsonProperty("rowId") long rowId,
                                             @JsonProperty("case_allocator") boolean caseAllocator,
                                             @JsonProperty("task_supervisor") boolean taskSupervisor) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailId = emailId;
        this.regionId = regionId;
        this.userType = userType;
        this.suspended = suspended;
        this.idamRoles = idamRoles;
        this.roles = roles;
        this.baseLocations = baseLocations;
        this.workerWorkAreaRequests = workerWorkAreaRequests;
        this.region = region;
        this.rowId = rowId;
        this.caseAllocator = caseAllocator;
        this.taskSupervisor = taskSupervisor;
    }
}
