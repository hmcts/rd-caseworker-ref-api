package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRefDataUserTypesResponse {
    @JsonProperty("user_type")
    private List<StaffRefDataUserType> userTypes;
}
