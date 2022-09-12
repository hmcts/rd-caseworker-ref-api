package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleAssignmentQueryResponse {
    @JsonProperty
    List<RoleAssignment> roleAssignmentResponse;
}
