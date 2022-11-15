package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateEmail;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateStaffProfileChildren;

import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_MISSING_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_MISSING_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NAME_REGEX;

@Getter
@Setter
@Builder(builderMethodName = "staffProfileCreationRequest")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
@ValidateStaffProfileChildren
public class StaffProfileCreationRequest {

    @JsonProperty("email_id")
    @ValidateEmail(message = CaseWorkerConstants.INVALID_EMAIL)
    @NotEmpty(message = CaseWorkerConstants.INVALID_EMAIL)
    private String emailId;

    @JsonProperty("first_name")
    @Pattern(regexp = NAME_REGEX, message = FIRST_NAME_INVALID)
    @NotEmpty(message = FIRST_NAME_MISSING_PROFILE)
    private String firstName;

    @JsonProperty("last_name")
    @Pattern(regexp = NAME_REGEX, message = LAST_NAME_INVALID)
    @NotEmpty(message = LAST_NAME_MISSING_PROFILE)
    private String lastName;

    @JsonProperty("services")
    @NotEmpty(message = CaseWorkerConstants.NO_WORK_AREA_PRESENT_PROFILE)
    private List<CaseWorkerServicesRequest> services;

    @JsonProperty("region")
    @NotEmpty(message = CaseWorkerConstants.MISSING_REGION_PROFILE)
    private String region;

    @JsonProperty("region_id")
    private Integer regionId;

    @JsonProperty("roles")
    @NotEmpty(message = CaseWorkerConstants.NO_ROLE_PRESENT_PROFILE)
    private List<StaffProfileRoleRequest> roles;

    @JsonProperty("task_supervisor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean taskSupervisor;

    @JsonProperty("suspended")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean suspended;

    @JsonProperty("case_allocator")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean caseAllocator;

    @JsonProperty("staff_admin")
    private boolean staffAdmin;

    @JsonProperty("base_locations")
    @NotEmpty(message = CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT_PROFILE)
    private List<CaseWorkerLocationRequest> baseLocations;

    @JsonProperty("user_type")
    @NotEmpty(message = CaseWorkerConstants.NO_USER_TYPE_PRESENT_PROFILE)
    private String userType;

    @JsonProperty("skills")
    private List<SkillsRequest> skills;

    @JsonProperty("idam_roles")
    private Set<String> idamRoles;
}
