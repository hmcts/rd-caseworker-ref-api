package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseWorkerProfile extends CaseWorkerDomain implements Serializable {

    private static final long serialVersionUID = 2019L;
    public static final String NO_LOCATION_PRESENT = "You must add location details and upload the file again.";
    public static final String NO_ROLE_PRESENT = "You must add role details and upload the file again";
    public static final String NO_WORK_AREA_PRESENT = "You must add details of at least one area of work and upload the file again";
    public static final String NO_USER_TYPE_PRESENT = "You must add a user type and upload the file again";
    public static final String FIRST_NAME_MISSING = "You must add a first name and upload the file again";
    public static final String LAST_NAME_MISSING = "You must add a last name and upload the file again";
    public static final String INVALID_EMAIL = "You must add a valid justice.gov.uk email address and upload the file again";
    public static final String MISSING_REGION = "You must add a region and upload the file again";

    private String id;

    @MappingField(columnName = "FIRST NAME")
    @NotEmpty(message = FIRST_NAME_MISSING)
    private String firstName;

    @MappingField(columnName = "LAST NAME")
    @NotEmpty(message = LAST_NAME_MISSING)
    private String lastName;

    @MappingField(columnName = "Official Email", position = 1)
    @Email(message = CaseWorkerConstants.INVALID_EMAIL)
    @Pattern(regexp = CaseWorkerConstants.USER_NAME_PATTERN + "@"
            + CaseWorkerConstants.DOMAIN_JUSTICE_GOV_UK,
            message = CaseWorkerConstants.INVALID_EMAIL)
    @NotEmpty(message = INVALID_EMAIL)
    @JsonProperty("email_id")
    private String officialEmail;

    @MappingField(columnName = "Region Id")
    @NotNull(message = MISSING_REGION)
    private int regionId;

    @MappingField(columnName = "Region")
    @NotEmpty(message = MISSING_REGION)
    @JsonProperty("region")
    private String regionName;

    @MappingField(clazz = Location.class, objectCount = 2)
    @NotEmpty(message = NO_LOCATION_PRESENT)
    @JsonProperty("base_location")
    private List<Location> locations;

    @JsonProperty("user_type_id")
    private Long userId;

    @MappingField(columnName = "User type")
    @NotEmpty(message = NO_USER_TYPE_PRESENT)
    private String userType;

    @MappingField(clazz = Role.class, objectCount = 2)
    @NotEmpty(message = NO_ROLE_PRESENT)
    @JsonProperty("role")
    private List<Role> roles;

    @MappingField(clazz = WorkArea.class, objectCount = 8)
    @NotEmpty(message = NO_WORK_AREA_PRESENT)
    @JsonProperty("work_area")
    private List<WorkArea> workAreas;

    @MappingField(columnName = "IDAM Roles")
    private String idamRoles;

    @MappingField(columnName = "Suspended")
    private String suspended;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}