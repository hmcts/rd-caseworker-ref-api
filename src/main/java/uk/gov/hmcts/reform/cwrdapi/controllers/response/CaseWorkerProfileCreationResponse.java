package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CaseWorkerProfileCreationResponse {

    private String caseWorkerRegistrationResponse;

    private String id;

    public CaseWorkerProfileCreationResponse(String caseWorkerRegistrationResponse) {
        this.caseWorkerRegistrationResponse = caseWorkerRegistrationResponse;
    }

    public CaseWorkerProfileCreationResponse(String caseWorkerRegistrationResponse, String id) {

        this.caseWorkerRegistrationResponse = caseWorkerRegistrationResponse;
        this.id = id;
    }
}
