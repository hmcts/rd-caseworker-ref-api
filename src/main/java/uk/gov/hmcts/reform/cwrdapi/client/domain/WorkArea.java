package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@NoArgsConstructor
public class WorkArea {

    @MappingField(columnName = "Aow1 Service Code,Aow2 Service Code,Aow3 Service Code,Aow4 Service Code,"
        + "Aow5 Service Code, Aow6 Service Code,Aow7 Service Code,Aow8 Service Code")
    Integer serviceCode;

    @MappingField(columnName = "Area of Work1,Area of Work2,Area of Work3,Area of Work4,Area of Work5,Area of Work6,"
        + "Area of Work7,Area of Work8")
    String areaOfWork;
}
