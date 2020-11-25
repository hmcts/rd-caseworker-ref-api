package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CaseWorkerRoleRequest {

    @JsonProperty("role")
    private String role;
    @JsonProperty("is_primary")
    private boolean isPrimaryFlag;

    @JsonCreator
    public CaseWorkerRoleRequest(@JsonProperty("role") String role,
                          @JsonProperty("is_primary")boolean isPrimaryFlag) {

        this.role = role;
        this.isPrimaryFlag = isPrimaryFlag;
    }
}
