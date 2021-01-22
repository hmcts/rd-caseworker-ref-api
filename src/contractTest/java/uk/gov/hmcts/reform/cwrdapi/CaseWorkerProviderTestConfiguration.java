package uk.gov.hmcts.reform.cwrdapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;

@TestConfiguration
public class CaseWorkerProviderTestConfiguration {

    @Bean
    @Primary
    public CaseWorkerServiceImpl getCaseWorkerServiceImpl() {
        return new CaseWorkerServiceImpl();
    }

    @MockBean
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @MockBean
    private RoleTypeRepository roleTypeRepository;

    @MockBean
    private UserTypeRepository userTypeRepository;

    @MockBean
    private CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @MockBean
    private IdamRoleMappingService idamRoleMappingService;

    @MockBean
    private UserProfileFeignClient userProfileFeignClient;

    @MockBean
    private TopicPublisher topicPublisher;

}
