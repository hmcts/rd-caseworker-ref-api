package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CaseWorkerProfilesDeletionResponse {

    private int statusCode;
    private String message;

    public CaseWorkerProfilesDeletionResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;

    }
}
