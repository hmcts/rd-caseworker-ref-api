package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersLocationRequest")
public class CaseWorkerLocationRequest {

    private Integer locationId;
    private String location;
    private boolean isPrimaryFlag;

}
