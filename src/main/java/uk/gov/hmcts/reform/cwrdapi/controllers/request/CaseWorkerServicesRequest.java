package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder(builderMethodName = "caseWorkerServicesRequest")
public class CaseWorkerServicesRequest {

    private String service;
    private String serviceCode;

    @JsonCreator
    public CaseWorkerServicesRequest(@JsonProperty("service") String service,
                                     @JsonProperty("service_code") String serviceCode) {
        this.service = service;
        this.serviceCode = serviceCode;
    }
}
