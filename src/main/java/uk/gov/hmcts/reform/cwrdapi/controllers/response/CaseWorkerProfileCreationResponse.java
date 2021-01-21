package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseWorkerProfileCreationResponse {
    @JsonProperty("message")
    private String caseWorkerRegistrationResponse;
    @JsonProperty("message_details")
    private String messageDetails;
    @JsonProperty("case_worker_ids")
    private List<String> caseWorkerIds;
}
