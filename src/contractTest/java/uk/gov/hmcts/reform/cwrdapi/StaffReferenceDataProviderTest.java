package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.CaseWorkerRefUsersController;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.StaffReferenceInternalController;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.LrdOrgInfoServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("referenceData_caseworkerRefUsers")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors = {
        @VersionSelector(tag = "master")})
@Import(CaseWorkerProviderTestConfiguration.class)
@SpringBootTest(properties = {"crd.publisher.caseWorkerDataPerMessage=1"})
@IgnoreNoPactsToVerify
public class StaffReferenceDataProviderTest {

    @Autowired
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @Autowired
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @MockBean
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @MockBean
    private LocationReferenceDataFeignClient locationReferenceDataFeignClient;

    @Autowired
    private DataSource ds;

    @Mock
    private CaseWorkerServiceFacade caseWorkerServiceFacade;

    private static final String USER_ID = "234873";
    private static final String USER_ID2 = "234879";

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(
                new CaseWorkerRefUsersController(
                        "RD-Caseworker-Ref-Api", 20, "caseWorkerId",
                        "preview", caseWorkerServiceImpl),
                new StaffReferenceInternalController(
                        "RD-Caseworker-Ref-Api", 20, "caseWorkerId",
                        caseWorkerServiceImpl)
        );
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @State({"A list of users for CRD request"})
    public void fetchListOfUsersById() {
        List<CaseWorkerProfile> caseWorkerProfile = Collections.singletonList(getCaseWorkerProfile(USER_ID));
        doReturn(caseWorkerProfile).when(caseWorkerProfileRepo).findByCaseWorkerIdIn(anyList());
    }

    @State({"A list of multiple users for CRD request"})
    public void fetchListOfMultipleUsersById() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(getCaseWorkerProfile(USER_ID));
        caseWorkerProfiles.add(getCaseWorkerProfile(USER_ID2));
        List<String> userRequest = Arrays.asList(USER_ID, USER_ID2);
        doReturn(caseWorkerProfiles).when(caseWorkerProfileRepo).findByCaseWorkerIdIn(userRequest);
    }

    @State({"A list of staff profiles for CRD request by service names"})
    public void fetchListOfStaffProfilesByServiceNames() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        LrdOrgInfoServiceResponse lrdOrgInfoServiceResponse = new LrdOrgInfoServiceResponse();
        lrdOrgInfoServiceResponse.setServiceCode("BFA1");
        lrdOrgInfoServiceResponse.setCcdServiceName("CMC");
        String body = mapper.writeValueAsString(List.of(lrdOrgInfoServiceResponse));

        when(locationReferenceDataFeignClient.getLocationRefServiceMapping(any()))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());

        CaseWorkerProfile caseWorkerProfile = getCaseWorkerProfile(USER_ID);
        CaseWorkerWorkArea caseWorkerWorkArea  = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerId("cwId");
        caseWorkerWorkArea.setServiceCode("BFA1");
        caseWorkerWorkArea.setCaseWorkerProfile(caseWorkerProfile);
        caseWorkerProfile.setCaseWorkerWorkAreas(List.of(caseWorkerWorkArea));
        PageImpl<CaseWorkerProfile> page = new PageImpl<>(List.of(caseWorkerProfile));
        doReturn(page).when(caseWorkerProfileRepo).findByServiceCodeIn(anySet(), any());
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
                "National",
                1,
                false,
                false,
                false,
                timeNow,
                timeNow,
                caseWorkerLocations,
                caseWorkerWorkAreas,
                caseWorkerRoles,
                new UserType(1L, "HMCTS"), false);
    }

}


