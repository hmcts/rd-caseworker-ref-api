package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(builderMethodName = "withIdBuilder")
public class SearchStaffUserByIdResponse extends SearchStaffUserResponse {

    @JsonProperty("up_idam_status")
    private String idamStatus;

}
