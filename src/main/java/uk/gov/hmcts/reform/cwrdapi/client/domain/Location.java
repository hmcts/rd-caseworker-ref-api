package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@Builder
@Getter
@Setter
public class Location {

    @MappingField(columnName = "Base location 1 id,Base location 2 id")
    @JsonProperty("location_id")
    private int baseLocationId;

    @MappingField(columnName = {"Primary Base Location Name", "Secondary Location"})
    @JsonProperty("location")
    private String locationName;

    @JsonProperty("is_primary")
    private boolean isPrimary;
}
