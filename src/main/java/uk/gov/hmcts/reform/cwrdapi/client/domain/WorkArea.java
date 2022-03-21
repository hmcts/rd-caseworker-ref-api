package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WorkArea implements Serializable {

    private static final long serialVersionUID = 2021L;

    @MappingField(columnName = "Service1 ID,Service2 ID,Service3 ID,Service4 ID,"
        + "Service5 ID, Service6 ID,Service7 ID,Service8 ID")
    String serviceCode;

    @MappingField(columnName = "Service1,Service2,Service3,Service4,Service5,Service6,"
        + "Service7,Service8")
    String areaOfWork;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}
