package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CaseWorkerProfileCreationResponse {

    private String caseWorkerRegistrationResponse;

    @JsonIgnore
    private List<String> caseWorkerIds;

    public CaseWorkerProfileCreationResponse(String caseWorkerRegistrationResponse,
                                             List<String> caseWorkerIds) {
        this.caseWorkerRegistrationResponse = caseWorkerRegistrationResponse;
        this.caseWorkerIds = caseWorkerIds;
    }
}
