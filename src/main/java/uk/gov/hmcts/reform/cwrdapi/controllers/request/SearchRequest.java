package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public class SearchRequest {

    @ApiParam(name = "serviceCode", value = "Any Valid String is allowed")
    String serviceCode;

    @ApiParam(name = "location", value = "Any Valid String is allowed with comma seperated values")
    String location;

    @ApiParam(name = "userType", value = "Any Valid String is allowed")
    String userType;

    @ApiParam(name = "jobTitle", value = "Any Valid String is allowed")
    String jobTitle;

    @ApiParam(name = "skill", value = "Any Valid String is allowed")
    String skill;

    @ApiParam(name = "role", value = "Any Valid String is allowed")
    String role;

}
