package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserProfileUpdatedData {

    private String idamStatus;

    @JsonCreator
    public UserProfileUpdatedData(@JsonProperty(value = "idamStatus") String idamStatus) {
        this.idamStatus = idamStatus;
    }

}
