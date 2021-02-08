package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.config.TrimStringFields;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkerWorkAreaRequest")
public class CaseWorkerWorkAreaRequest {

    @JsonDeserialize(using = TrimStringFields.class)
    private String areaOfWork;
    @JsonDeserialize(using = TrimStringFields.class)
    private String serviceCode;

    @JsonCreator
    public CaseWorkerWorkAreaRequest(@JsonProperty("area_of_work") String areaOfWork,
                              @JsonProperty("service_code") String serviceCode) {
        this.areaOfWork = areaOfWork;
        this.serviceCode = serviceCode;
    }
}
