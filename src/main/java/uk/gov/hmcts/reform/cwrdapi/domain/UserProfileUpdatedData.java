package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class UserProfileUpdatedData {

    private String idamStatus;

    private Set<RoleName> rolesAdd;
}
