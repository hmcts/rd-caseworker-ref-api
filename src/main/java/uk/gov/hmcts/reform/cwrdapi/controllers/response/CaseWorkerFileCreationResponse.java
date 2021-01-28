package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CaseWorkerFileCreationResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("message_details")
    private String detailedMessage;

    @JsonProperty("error_details")
    private List<JsrFileErrors> errorDetails;
}
