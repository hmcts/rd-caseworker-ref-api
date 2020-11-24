package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileCreationResponse {

    private String idamId;
    private Integer idamRegistrationResponse;

    public void setIdamRegistrationResponse(Integer idamRegistrationResponse) {
        this.idamRegistrationResponse = idamRegistrationResponse;
    }
}
