package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewUserResponse {

    @JsonProperty
    private  String userIdentifier;
    @JsonProperty
    private String  idamStatus;

    //    public NewUserResponse(ProfessionalUser user) {
    //
    //        this.userIdentifier = user.getUserIdentifier();
    //    }

    public NewUserResponse(UserProfileCreationResponse userProfileCreationResponse) {
        this.userIdentifier = userProfileCreationResponse.getIdamId();
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }
}