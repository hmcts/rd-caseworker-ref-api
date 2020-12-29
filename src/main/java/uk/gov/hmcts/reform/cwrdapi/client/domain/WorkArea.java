package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkArea implements Serializable {

    private static final long serialVersionUID = 2021L;

    @MappingField(columnName = "Aow1 Service Code,Aow2 Service Code,Aow3 Service Code,Aow4 Service Code,"
        + "Aow5 Service Code, Aow6 Service Code,Aow7 Service Code,Aow8 Service Code")
    String serviceCode;

    @MappingField(columnName = "Area of Work1,Area of Work2,Area of Work3,Area of Work4,Area of Work5,Area of Work6,"
        + "Area of Work7,Area of Work8")
    String areaOfWork;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdateTime;
}
