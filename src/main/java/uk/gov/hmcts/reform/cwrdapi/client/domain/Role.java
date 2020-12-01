package uk.gov.hmcts.reform.cwrdapi.client.domain;

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
    private String roleName;

    private boolean isPrimary;
}
