package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateCaseWorkerChildren;
import uk.gov.hmcts.reform.cwrdapi.util.ValidateEmail;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_MISSING;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_MISSING;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NAME_REGEX;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ValidateCaseWorkerChildren
public class CaseWorkerProfile extends CaseWorkerDomain implements Serializable {

    private static final long serialVersionUID = 2019L;

    private String id;

    @MappingField(columnName = "First Name")
    @Pattern(regexp = NAME_REGEX, message = FIRST_NAME_INVALID)
    @NotEmpty(message = FIRST_NAME_MISSING)
    @Schema(name = "firstName",  example = "string")
    private String firstName;

    @MappingField(columnName = "Last Name")
    @Pattern(regexp = NAME_REGEX, message = LAST_NAME_INVALID)
    @NotEmpty(message = LAST_NAME_MISSING)
    @Schema(name = "lastName",  example = "string")
    private String lastName;

    @MappingField(columnName = "Email", position = 1)
    @ValidateEmail(message = CaseWorkerConstants.INVALID_EMAIL)
    @NotEmpty(message = CaseWorkerConstants.INVALID_EMAIL)
    @JsonProperty("email_id")
    private String officialEmail;

    @MappingField(columnName = "Region ID")
    private Integer regionId;

    @MappingField(columnName = "Region")
    @NotEmpty(message = CaseWorkerConstants.MISSING_REGION)
    @JsonProperty("region")
    private String regionName;

    @MappingField(clazz = Location.class, objectCount = 2)
    @JsonProperty("base_location")
    private List<Location> locations;

    @JsonProperty("user_type_id")
    private Long userId;

    @MappingField(columnName = "User type")
    @NotEmpty(message = CaseWorkerConstants.NO_USER_TYPE_PRESENT)
    private String userType;

    @MappingField(clazz = Role.class, objectCount = 2)
    @NotEmpty(message = CaseWorkerConstants.NO_ROLE_PRESENT)
    @JsonProperty("role")
    private List<Role> roles;

    @JsonProperty("skills")
    private List<Skill> skills;

    @MappingField(clazz = WorkArea.class, objectCount = 8)
    @NotEmpty(message = CaseWorkerConstants.NO_WORK_AREA_PRESENT)
    @JsonProperty("work_area")
    private List<WorkArea> workAreas;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @MappingField(columnName = "IDAM Roles")
    private String idamRoles;

    @MappingField(columnName = "Suspended")
    private String suspended;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @MappingField(columnName = "Case Allocator")
    private String caseAllocator;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @MappingField(columnName = "Task Supervisor")
    private String taskSupervisor;

    private String staffAdmin;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}