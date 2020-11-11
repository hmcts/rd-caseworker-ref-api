package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class ErrorResponse {

    private int errorCode;

    private String status;

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;

    public ErrorResponse(int errorCode, String status,String errorMessage, String errorDescription,
                         String timeStamp) {
        this.errorCode = errorCode;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.timeStamp = timeStamp;
    }
}
