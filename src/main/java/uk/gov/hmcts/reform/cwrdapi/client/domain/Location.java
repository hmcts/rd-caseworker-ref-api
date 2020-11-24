package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

@NoArgsConstructor
@Getter
@Setter
public class Location {

    @MappingField(columnName = "Base location 1 id,Base location 2 id")
    private Integer locationId;

    @MappingField(columnName = "Primary Base Location Name,Secondary Location",
            isPrimary = "Primary Base Location Name")
    private String locationName;

    private boolean isPrimary;
}

