package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.config.TrimStringFields;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkerRoleRequest")
public class CaseWorkerRoleRequest {

    @JsonDeserialize(using = TrimStringFields.class)
    private String role;
    private boolean isPrimaryFlag;

    @JsonCreator
    public CaseWorkerRoleRequest(@JsonProperty("role") String role,
                          @JsonProperty("is_primary")boolean isPrimaryFlag) {

        this.role = role;
        this.isPrimaryFlag = isPrimaryFlag;
    }
}
