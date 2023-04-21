package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIdentifier {
    private String id;
    private String uid;
    private String forename;
    private String surname;
    private String email;
    private String accountStatus;
    private List<String> roles;
    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String idamStatus;

}