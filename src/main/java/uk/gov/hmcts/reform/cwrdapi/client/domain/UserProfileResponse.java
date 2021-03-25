package uk.gov.hmcts.reform.cwrdapi.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileResponse {

    @JsonProperty ("userIdentifier")
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private String idamStatus;
    private List<String> roles;
    private String idamStatusCode;
}

