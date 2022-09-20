package uk.gov.hmcts.reform.cwrdapi.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;

@Service
public interface StaffRefDataService {
    List<SearchStaffUserResponse> retrieveStaffUserByName(String searchString, PageRequest pageRequest);
}
