package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@Builder(builderMethodName = "anUserProfileCreationRequest")
public class UserProfileCreationRequest  {

    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private LanguagePreference languagePreference;

    @NotBlank
    private UserCategory userCategory;

    @NotBlank
    private UserType userType;

    @NotEmpty
    private Set<String> roles;

    private boolean resendInvite;

    @JsonCreator
    public UserProfileCreationRequest(@JsonProperty(value = "email") String email,
                                      @JsonProperty(value = "firstName") String firstName,
                                      @JsonProperty(value = "lastName") String lastName,
                                      @JsonProperty(value = "languagePreference") LanguagePreference languagePreference,
                                      @JsonProperty(value = "userCategory") UserCategory userCategory,
                                      @JsonProperty(value = "userType") UserType userType,
                                      @JsonProperty(value = "roles") Set<String> roles,
                                      @JsonProperty(value = "resendInvite") boolean resendInvite) {

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languagePreference = languagePreference;
        this.userCategory = userCategory;
        this.userType = userType;
        this.roles = roles;
        this.resendInvite = resendInvite;
    }
}
