package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

import java.util.List;

@Service
public interface StaffRefDataService {
    List<UserType> fetchUserTypes();

    List<RoleType> getJobTitles();
}
