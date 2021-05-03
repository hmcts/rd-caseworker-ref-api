package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LrdOrgInfoServiceResponse {

    private Long serviceId;

    private String orgUnit;

    private String businessArea;

    private String subBusinessArea;

    private String jurisdiction;

    private String serviceDescription;

    private String serviceCode;

    private String serviceShortDescription;

    private String ccdServiceName;

    private String lastUpdate;

    private List<String> ccdCaseTypes;

}
