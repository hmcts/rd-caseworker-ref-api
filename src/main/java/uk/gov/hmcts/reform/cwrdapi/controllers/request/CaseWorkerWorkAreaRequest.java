package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkerWorkAreaRequest")
public class CaseWorkerWorkAreaRequest {

    private String areaOfWork;
    private String serviceCode;

    @JsonCreator
    CaseWorkerWorkAreaRequest(@JsonProperty("area_of_work") String areaOfWork,
                              @JsonProperty("service_code") String serviceCode) {
        this.areaOfWork = areaOfWork;
        this.serviceCode = serviceCode;
    }
}
