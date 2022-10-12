package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor

public class SearchRequest {

    @ApiParam(name = "serviceId", value = "Any Valid String is allowed")
    String serviceId;

    @ApiParam(name = "locationId", value = "Any Valid String is allowed with comma seperated values")
    String locationId;

    @ApiParam(name = "userTypeId", value = "Any Valid String is allowed")
    String userTypeId;

    @ApiParam(name = "jobTitleId", value = "Any Valid String is allowed")
    String jobTitleId;

    @ApiParam(name = "skillId", value = "Any Valid String is allowed")
    String skillId;

    @ApiParam(name = "roles", value = "Any Valid String is allowed")
    String roles;


}
