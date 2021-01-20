package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRoleMapping extends CaseWorkerDomain {

    @MappingField(columnName = "Service ID")
    @NotEmpty
    @JsonProperty(value = "service_code")
    String serivceId;

    @MappingField(columnName = "Roles")
    @NotNull
    @JsonProperty(value = "role")
    int roleId;

    @MappingField(columnName = "Idam Roles")
    @NotEmpty
    @JsonProperty(value = "idam_roles")
    String idamRoles;
}
