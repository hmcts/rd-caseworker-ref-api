package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.config.TrimStringFields;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(builderMethodName = "staffProfileRoleRequest")
public class StaffProfileRoleRequest {

    @JsonProperty("role_id")
    private Integer roleId;

    @JsonProperty("role")
    @JsonDeserialize(using = TrimStringFields.class)
    private String role;

    @JsonProperty("is_primary")
    private boolean isPrimaryFlag;

    @JsonCreator
    public StaffProfileRoleRequest(@JsonProperty("role_id") Integer roleId,
                                   @JsonProperty("role") String role,
                                   @JsonProperty("is_primary")boolean isPrimaryFlag) {
        this.roleId = roleId;
        this.role = role;
        this.isPrimaryFlag = isPrimaryFlag;
    }
}
