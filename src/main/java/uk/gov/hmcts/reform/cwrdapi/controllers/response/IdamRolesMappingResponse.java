package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdamRolesMappingResponse {
    @JsonProperty
    private int statusCode;
    @JsonProperty
    private String message;
}
