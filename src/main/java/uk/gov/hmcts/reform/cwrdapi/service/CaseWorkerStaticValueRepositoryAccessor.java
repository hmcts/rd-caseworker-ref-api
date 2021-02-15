package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;

public interface CaseWorkerStaticValueRepositoryAccessor {
    /**
     * Get the role types from the static role type table.
     * @return role types
     */
    List<RoleType> getRoleTypes();

    /**
     * Get the user types from the static user type table.
     * @return user types
     */
    List<UserType> getUserTypes();
}
