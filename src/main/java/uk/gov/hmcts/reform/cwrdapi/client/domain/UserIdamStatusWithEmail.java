package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserIdamStatusWithEmail {


    private String email;
    private String idamStatus;



    public UserIdamStatusWithEmail(String email, String idamStatus) {
        this.email = email;
        this.idamStatus = idamStatus;
    }
}
