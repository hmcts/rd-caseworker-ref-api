package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@NoArgsConstructor
@Getter
@Setter
public class Role {

    private Integer roleId;

    @MappingField(columnName = "Primary Role,Secondary Role", isPrimary = "Primary Role")
    private String role;

    private boolean isPrimary;
}
