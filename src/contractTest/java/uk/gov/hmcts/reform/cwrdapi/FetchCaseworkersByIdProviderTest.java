package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import au.com.dius.pact.provider.junitsupport.Provider;
import uk.gov.hmcts.reform.cwrdapi.controllers.CaseWorkerRefController;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Mock
    ExcelValidatorService excelValidatorService;

    @Mock
    ExcelAdaptorService excelAdaptorService;


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
                new CaseWorkerRefController(caseWorkerServiceImpl, excelValidatorService, excelAdaptorService));
        context.setTarget(testTarget);
        MockitoAnnotations.openMocks(this);
        setInitiMock();
    }

    @State({"A list of users for CRD request"})
    public void fetchListOfUsersById() {
        //state
    }

    @State({"A list of multiple users for CRD request"})
    public void fetchListOfMultipleUsersById() {
        //state
    }

    private void setInitiMock() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(getCaseWorkerProfile("21334a2b-79ce-44eb-9168-2d49a744be9c"));
        caseWorkerProfiles.add(getCaseWorkerProfile("21334a2b-79ce-44eb-9168-2d49a799be9d"));

        when(caseWorkerProfileRepo.findByCaseWorkerIdIn(anyList()))
                .thenReturn(caseWorkerProfiles);

    }

    private CaseWorkerProfile getCaseWorkerProfile(String caseWorkerId) {
        LocalDateTime timeNow = LocalDateTime.now();

        List<CaseWorkerLocation> caseWorkerLocations =
                Collections.singletonList(new CaseWorkerLocation(caseWorkerId, 1,
                        "location", true));

        List<CaseWorkerWorkArea> caseWorkerWorkAreas =
                Collections.singletonList(new CaseWorkerWorkArea(caseWorkerId, "1", "BFA1"));

        List<CaseWorkerRole> caseWorkerRoles =
                Collections.singletonList(new CaseWorkerRole(1L, caseWorkerId, 1L, false, timeNow, timeNow,
                        new CaseWorkerProfile(), new RoleType()));

        return new CaseWorkerProfile(caseWorkerId,
                "firstName",
                "lastName",
                "email@email.gov",
                1L,
                "National",
                1,
                false,
                timeNow.plusDays(45L),
                timeNow,
                timeNow,
                caseWorkerLocations,
                caseWorkerWorkAreas,
                caseWorkerRoles,
                new UserType());
    }

}


