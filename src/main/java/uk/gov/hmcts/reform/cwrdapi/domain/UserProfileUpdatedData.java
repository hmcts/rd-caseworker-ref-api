package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserProfileUpdatedData {

    private String idamStatus;
    private Set<RoleName> rolesDelete;
    private Set<RoleName> rolesAdd;
    private String firstName;
    private String lastName;
}
