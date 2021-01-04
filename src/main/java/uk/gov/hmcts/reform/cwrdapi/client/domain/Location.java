package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Location implements Serializable {

    private static final long serialVersionUID = 2019L;

    @MappingField(columnName = "Base location 1 id,Base location 2 id")
    @JsonProperty("location_id")
    private Integer baseLocationId;

    @MappingField(columnName = "Primary Base Location Name,Secondary Location",
            isPrimary = "Primary Base Location Name")
    @JsonProperty("location")
    private String locationName;
    private boolean isPrimary;

    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;
}

