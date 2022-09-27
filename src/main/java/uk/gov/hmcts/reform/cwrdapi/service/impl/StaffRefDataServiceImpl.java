package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;

@Service
@Slf4j
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Override
    public List<RoleType> getJobTitles() {
        return roleTypeRepository.findAll();
    }


}
