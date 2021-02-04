package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WorkArea implements Serializable {

    private static final long serialVersionUID = 2021L;

    @MappingField(columnName = "Area of Work1 ID,Area of Work2 ID,Area of Work3 ID,Area of Work4 ID,"
        + "Area of Work5 ID, Area of Work6 ID,Area of Work7 ID,Area of Work8 ID")
    String serviceCode;

    @MappingField(columnName = "Area of Work1,Area of Work2,Area of Work3,Area of Work4,Area of Work5,Area of Work6,"
        + "Area of Work7,Area of Work8")
    String areaOfWork;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}
