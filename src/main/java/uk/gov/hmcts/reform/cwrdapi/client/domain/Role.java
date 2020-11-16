package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@NoArgsConstructor
@Getter
@Setter
public class Role {

    private int roleId;

    @MappingField(columnName = "Primary Role,Secondary Role")
    private String role;

    private boolean isPrimary;
}
