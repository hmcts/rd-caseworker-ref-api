package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerProfile extends CaseWorkerDomain implements Serializable {

    private static final long serialVersionUID = 2019L;

    private String id;

    @MappingField(columnName = "FIRST NAME")
    @NotEmpty
    @JsonProperty("first_name")
    private String firstName;

    @MappingField(columnName = "LAST NAME")
    @NotEmpty
    @JsonProperty("last_name")
    private String lastName;

    @MappingField(columnName = "Official Email")
    @NotEmpty
    @JsonProperty("email_id")
    private String officialEmail;

    @MappingField(columnName = "Region Id")
    @JsonProperty("region_id")
    private int regionId;

    @MappingField(columnName = "Region")
    @NotEmpty
    @JsonProperty("region")
    private String regionName;

    @MappingField(clazz = Location.class, objectCount = 2)
    @NotEmpty(message = "no primary or secondary location exists")
    @JsonProperty("base_location")
    private List<Location> locations;

    @JsonProperty("user_type_id")
    private Long userId;

    @MappingField(columnName = "User type")
    @NotEmpty
    @JsonProperty("user_type")
    private String userType;

    @MappingField(clazz = Role.class, objectCount = 2)
    @NotEmpty(message = "no primary or secondary roles exists")
    @JsonProperty("role")
    private List<Role> roles;

    @MappingField(clazz = WorkArea.class, objectCount = 8)
    @NotEmpty(message = "no area of works exists")
    @JsonProperty("work_area")
    private List<WorkArea> workAreas;

    @MappingField(columnName = "IDAM Roles")
    private String idamRoles;

    @MappingField(columnName = "Delete Flag")
    @JsonProperty("delete_flag")
    private String deleteFlag;

    @JsonProperty("created_time")
    private LocalDateTime createdTime;
    @JsonProperty("last_updated_time")
    private LocalDateTime lastUpdateTime;
}