package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Location implements Serializable {

    private static final long serialVersionUID = 2019L;

    @MappingField(columnName = "Base location 1 id,Base location 2 id")
    @JsonProperty("location_id")
    private Integer baseLocationId;

    @MappingField(columnName = "Primary Base Location Name,Secondary Location",
            isPrimary = "Primary Base Location Name")
    @JsonProperty("location")
    private String locationName;

    @JsonProperty("is_primary")
    private boolean isPrimary;

    @JsonProperty("created_time")
    private LocalDateTime createdTime;
    @JsonProperty("last_updated_time")
    private LocalDateTime lastUpdateTime;
}

