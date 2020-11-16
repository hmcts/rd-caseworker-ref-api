package uk.gov.hmcts.reform.cwrdapi.client.domain;

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
    private String roleName;

    private boolean isPrimary;
}
