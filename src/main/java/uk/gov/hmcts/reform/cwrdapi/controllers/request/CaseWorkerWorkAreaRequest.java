package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkerWorkAreaRequest")
public class CaseWorkerWorkAreaRequest {

    private String areaOfWork;
    private String serviceCode;
}
