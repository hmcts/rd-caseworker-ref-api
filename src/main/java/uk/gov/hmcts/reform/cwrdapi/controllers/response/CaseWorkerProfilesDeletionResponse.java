package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaseWorkerProfilesDeletionResponse {

    private int statusCode;
    private String message;

}
