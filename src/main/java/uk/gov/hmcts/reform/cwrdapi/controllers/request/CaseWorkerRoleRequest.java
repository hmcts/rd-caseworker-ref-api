package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkerRoleRequest")
public class CaseWorkerRoleRequest {

    private String role;
    private boolean isPrimaryFlag;

    @JsonCreator
    CaseWorkerRoleRequest(@JsonProperty("role") String role,
                          @JsonProperty("is_primary")boolean isPrimaryFlag) {

        this.role = role;
        this.isPrimaryFlag = isPrimaryFlag;
    }
}
