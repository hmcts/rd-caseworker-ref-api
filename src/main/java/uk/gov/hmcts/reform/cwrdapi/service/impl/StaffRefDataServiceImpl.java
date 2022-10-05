package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;

@Service
@Slf4j
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Override
    public List<UserType> fetchUserTypes() {
        return userTypeRepository
                .findAll();
    }

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Override
    public List<RoleType> getJobTitles() {
        return roleTypeRepository.findAll();
    }

    @Override
    public ResponseEntity<Object> retrieveStaffProfile(SearchRequest searchRequest, Object pageRequest) {
        return null;
    }


}
