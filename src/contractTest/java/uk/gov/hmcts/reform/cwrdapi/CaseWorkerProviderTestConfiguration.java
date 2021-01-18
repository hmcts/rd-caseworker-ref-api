package uk.gov.hmcts.reform.cwrdapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;

@TestConfiguration
public class CaseWorkerProviderTestConfiguration {

    @Bean
    @Primary
    public CaseWorkerServiceImpl getCaseWorkerServiceImpl() {
        return new CaseWorkerServiceImpl();
    }

    @MockBean
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

}
