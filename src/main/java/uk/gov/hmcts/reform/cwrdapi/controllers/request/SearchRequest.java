package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class SearchRequest {

    @Parameter(name = "serviceCode", description = "Any Valid String is allowed with comma seperated values")
    String serviceCode;

    @Parameter(name = "location", description = "Any Valid String is allowed with comma seperated values")
    String location;

    @Parameter(name = "userType", description = "Any Valid String is allowed")
    String userType;

    @Parameter(name = "jobTitle", description = "Any Valid String is allowed")
    String jobTitle;

    @Parameter(name = "skill", description = "Any Valid String is allowed")
    String skill;

    @Parameter(name = "role", description = "Any Valid String is allowed with comma seperated values")
    String role;

}
