package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

@Service
@Slf4j
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

}
