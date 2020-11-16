package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import javax.validation.constraints.NotEmpty;

@Builder
@Getter
@Setter
public class ServiceRoleMapping extends CasWorkerDomain {

    @MappingField(columnName = "Service ID")
    @NotEmpty
    String serivceId;

    @MappingField(columnName = "Roles")
    @NotEmpty
    int roleId;

    @MappingField(columnName = "Idam Roles")
    @NotEmpty
    String idamRoles;
}
