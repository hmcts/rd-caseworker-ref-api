package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@Builder
@Getter
@Setter
public class Location {

    @MappingField(columnName = "Base location 1 id,Base location 2 id")
    private int baseLocationId;

    @MappingField(columnName = "Primary Base Location Name,Secondary Location")
    private String locationName;

    private boolean isPrimary;
}
