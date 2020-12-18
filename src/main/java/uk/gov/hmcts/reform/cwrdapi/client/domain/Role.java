package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private Integer roleId;

    @MappingField(columnName = "Primary Role,Secondary Role", isPrimary = "Primary Role")
    @JsonProperty("role")
    private String roleName;

    @JsonProperty("is_primary")
    private boolean isPrimary;
}
