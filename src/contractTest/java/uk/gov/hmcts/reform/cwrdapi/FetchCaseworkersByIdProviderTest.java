package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.CaseWorkerRefController;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@Provider("crd_case_worker_ref_service")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:9292}")
@Import(CaseWorkerProviderTestConfiguration.class)
@SpringBootTest(properties = { "crd.publisher.caseWorkerDataPerMessage=1" })
public class FetchCaseworkersByIdProviderTest {

    @Autowired
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @Autowired
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @Autowired
    private IdamRoleMappingService idamRoleMappingService;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    @Autowired
    private CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    private CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Mock
    private CaseWorkerServiceFacade caseWorkerServiceFacade;

    private static final String USER_ID = "234873";
    private static final String USER_ID2 = "234879";

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(
                new CaseWorkerRefController(
                        "RD-Caseworker-Ref-Api", caseWorkerServiceImpl, caseWorkerServiceFacade));
        context.setTarget(testTarget);
    }

    @State({"A list of users for CRD request"})
    public void fetchListOfUsersById() {
        List<CaseWorkerProfile> caseWorkerProfile = Collections.singletonList(getCaseWorkerProfile(USER_ID));
        List<String> userRequest = Collections.singletonList(USER_ID);
        doReturn(caseWorkerProfile).when(caseWorkerProfileRepo).findByCaseWorkerIdIn(userRequest);
    }

    @State({"A list of multiple users for CRD request"})
    public void fetchListOfMultipleUsersById() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(getCaseWorkerProfile(USER_ID));
        caseWorkerProfiles.add(getCaseWorkerProfile(USER_ID2));
        List<String> userRequest = Arrays.asList(USER_ID, USER_ID2);
        doReturn(caseWorkerProfiles).when(caseWorkerProfileRepo).findByCaseWorkerIdIn(userRequest);
    }

    private CaseWorkerProfile getCaseWorkerProfile(String caseWorkerId) {
        LocalDateTime timeNow = LocalDateTime.now();

        List<CaseWorkerLocation> caseWorkerLocations =
                Collections.singletonList(new CaseWorkerLocation(caseWorkerId, 1,
                        "National", true));

        List<CaseWorkerWorkArea> caseWorkerWorkAreas =
                Collections.singletonList(new CaseWorkerWorkArea(caseWorkerId, "1", "BFA1"));

        List<CaseWorkerRole> caseWorkerRoles =
                Collections.singletonList(new CaseWorkerRole(caseWorkerId, 1L, true));
        caseWorkerRoles.get(0).setRoleType(new RoleType("tribunal-caseworker"));

        return new CaseWorkerProfile(caseWorkerId,
                "firstName",
                "lastName",
                "sam.test@justice.gov.uk",
                1L,
                1,
                "National",
                false,
                timeNow,
                timeNow,
                caseWorkerLocations,
                caseWorkerWorkAreas,
                caseWorkerRoles,
                new UserType(1L, "HMCTS"));
    }

}


