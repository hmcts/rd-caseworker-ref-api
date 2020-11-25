package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CaseWorkerLocationRequest {

    @JsonProperty("location_id")
    private Integer locationId;
    @JsonProperty("location")
    private String location;
    @JsonProperty("is_primary")
    private boolean isPrimaryFlag;

    @JsonCreator
    public CaseWorkerLocationRequest(@JsonProperty("location_id") Integer locationId,
                              @JsonProperty("location") String location,
                              @JsonProperty("is_primary") boolean isPrimaryFlag) {

        this.locationId = locationId;
        this.location = location;
        this.isPrimaryFlag = isPrimaryFlag;
    }

}
