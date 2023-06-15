package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersProfileUpdationResponse")
@AllArgsConstructor
@NoArgsConstructor
public class CaseWorkersProfileUpdationResponse {

    @JsonProperty("case_worker_id")
    private String userId;
}
