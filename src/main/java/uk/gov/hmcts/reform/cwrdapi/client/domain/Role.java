package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@Builder
@Getter
@Setter
public class Role {

    private int roleId;

    @MappingField(columnName = "Primary Role,Secondary Role")
    @JsonProperty("role")
    private String roleName;

    @JsonProperty("is_primary")
    private boolean isPrimary;
}
