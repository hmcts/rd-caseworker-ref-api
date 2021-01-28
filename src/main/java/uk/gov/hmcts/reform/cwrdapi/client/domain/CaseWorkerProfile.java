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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CaseWorkerProfile extends CaseWorkerDomain implements Serializable {

    private static final long serialVersionUID = 2019L;


    private String id;

    @MappingField(columnName = "FIRST NAME")
    @NotEmpty(message = CaseWorkerConstants.FIRST_NAME_MISSING)
    private String firstName;

    @MappingField(columnName = "LAST NAME")
    @NotEmpty(message = CaseWorkerConstants.LAST_NAME_MISSING)
    private String lastName;

    @MappingField(columnName = "Official Email", position = 1)
    @Email(message = CaseWorkerConstants.INVALID_EMAIL)
    @Pattern(regexp = CaseWorkerConstants.USER_NAME_PATTERN + "@"
            + CaseWorkerConstants.DOMAIN_JUSTICE_GOV_UK,
            message = CaseWorkerConstants.INVALID_EMAIL)
    @NotEmpty(message = CaseWorkerConstants.INVALID_EMAIL)
    @JsonProperty("email_id")
    private String officialEmail;

    @MappingField(columnName = "Region Id")
    @NotNull(message = CaseWorkerConstants.MISSING_REGION)
    private int regionId;

    @MappingField(columnName = "Region")
    @NotEmpty(message = CaseWorkerConstants.MISSING_REGION)
    @JsonProperty("region")
    private String regionName;

    @MappingField(clazz = Location.class, objectCount = 2)
    @NotEmpty(message = CaseWorkerConstants.NO_LOCATION_PRESENT)
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

    @MappingField(clazz = WorkArea.class, objectCount = 8)
    @NotEmpty(message = CaseWorkerConstants.NO_WORK_AREA_PRESENT)
    @JsonProperty("work_area")
    private List<WorkArea> workAreas;

    @MappingField(columnName = "IDAM Roles")
    private String idamRoles;

    @MappingField(columnName = "Suspended")
    private String suspended;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}