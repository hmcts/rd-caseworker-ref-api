package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserProfileUpdatedData {

    private String idamStatus;

    private List<String> roles;
}
