package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseWorkerProfileResponseForRest {

    @JsonProperty
    List<ExceptionCaseWorker> exceptionCaseWorkers;
}

