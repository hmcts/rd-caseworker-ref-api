package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CaseWorkerWorkAreaRequest {

    @JsonProperty("area_of_work")
    private String areaOfWork;
    @JsonProperty("service_code")
    private String serviceCode;

    @JsonCreator
    public CaseWorkerWorkAreaRequest(@JsonProperty("area_of_work") String areaOfWork,
                              @JsonProperty("service_code") String serviceCode) {
        this.areaOfWork = areaOfWork;
        this.serviceCode = serviceCode;
    }
}
