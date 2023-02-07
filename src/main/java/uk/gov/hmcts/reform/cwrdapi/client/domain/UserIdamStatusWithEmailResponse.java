package uk.gov.hmcts.reform.cwrdapi.client.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserIdamStatusWithEmailResponse {
    private List<UserIdamStatusWithEmail> userProfiles = new ArrayList<>();

    public UserIdamStatusWithEmailResponse(List<UserProfileIdamStatus> userProfile) {
        this.userProfiles = userProfile.stream()
                .map(user -> new UserIdamStatusWithEmail(user.getEmail(), user.getStatus()))
                .toList();
    }
}