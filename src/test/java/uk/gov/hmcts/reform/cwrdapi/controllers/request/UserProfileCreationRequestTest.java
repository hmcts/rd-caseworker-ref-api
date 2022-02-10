package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileCreationRequestTest {

    Set<String> roles = new HashSet<>(Arrays.asList("caseworker"));

    @Test
    void testUserProfileCreationRequestWithBuilder() {
        UserProfileCreationRequest request = UserProfileCreationRequest.anUserProfileCreationRequest()
                .email("abc@temp.com")
                .firstName("first")
                .lastName("last")
                .languagePreference(LanguagePreference.EN)
                .userCategory(UserCategory.CASEWORKER)
                .userType(UserTypeRequest.INTERNAL)
                .roles(roles)
                .resendInvite(false)
                .build();
        verify(request);
    }

    @Test
    void testUserProfileCreationRequestWithConstrictor() {

        UserProfileCreationRequest request1 = new UserProfileCreationRequest(
                "abc@temp.com","first",
                "last",LanguagePreference.EN,
                UserCategory.CASEWORKER, UserTypeRequest.INTERNAL, roles,
                        false);
        verify(request1);
    }

    void verify(UserProfileCreationRequest request) {
        assertThat(request.getEmail()).isEqualTo("abc@temp.com");
        assertThat(request.getFirstName()).isEqualTo("first");
        assertThat(request.getLastName()).isEqualTo("last");
        assertThat(request.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(request.getUserCategory()).isEqualTo(UserCategory.CASEWORKER);
        assertThat(request.getUserType()).isEqualTo(UserTypeRequest.INTERNAL);
        assertThat(request.getRoles()).isEqualTo(roles);
        assertThat(request.isResendInvite()).isFalse();
    }
}
