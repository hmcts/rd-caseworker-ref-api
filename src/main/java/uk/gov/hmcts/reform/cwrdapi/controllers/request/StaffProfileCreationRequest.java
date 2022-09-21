package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder(builderMethodName = "staffProfileCreationRequest")
public class StaffProfileCreationRequest {

    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("services")
    private List<CaseWorkerServicesRequest> services;

    @JsonProperty("region")
    private String region;

    @JsonProperty("region_id")
    private Integer regionId;

    @JsonProperty("roles")
    private List<CaseWorkerRoleRequest> roles;

    @JsonProperty("task_supervisor")
    private boolean taskSupervisor;

    @JsonProperty("suspended")
    private boolean suspended;

    @JsonProperty("case_allocator")
    private boolean caseAllocator;

    @JsonProperty("staff_admin")
    private boolean staffAdmin;

    @JsonProperty("base_locations")
    private List<CaseWorkerLocationRequest> baseLocations;

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("skills")
    private List<SkillsRequest> skills;
}
