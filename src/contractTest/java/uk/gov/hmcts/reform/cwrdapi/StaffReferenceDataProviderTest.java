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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.CaseWorkerRefUsersController;
import uk.gov.hmcts.reform.cwrdapi.controllers.StaffRefDataController;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.StaffReferenceInternalController;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.LrdOrgInfoServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerStaticValueRepositoryAccessor;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerDeleteServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.StaffRefDataServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("referenceData_caseworkerRefUsers")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}", consumerVersionSelectors = {
        @VersionSelector(tag = "Dev")})
@IgnoreNoPactsToVerify
@ExtendWith(MockitoExtension.class)
public class StaffReferenceDataProviderTest {


    @Mock
    private RoleTypeRepository roleTypeRepository;
    @InjectMocks
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @InjectMocks
    private CaseWorkerDeleteServiceImpl caseWorkerDeleteServiceImpl;

    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Mock
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @MockBean
    private LocationReferenceDataFeignClient locationReferenceDataFeignClient;
    @Mock
    private CaseWorkerServiceFacade caseWorkerServiceFacade;

    @Mock
    CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;

    @Mock
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;
    @Mock
    private UserTypeRepository userTypeRepository;
    @MockBean
    private UserProfileFeignClient userProfileFeignClient;
    @Mock
    IJsrValidatorInitializer validateStaffProfile;
    @Mock
    StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;
    @Mock
    StaffAuditRepository staffAuditRepository;
    @Mock
    IValidationService validationServiceFacade;


    @Mock
    SkillRepository skillRepository;
    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;


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
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(
                new CaseWorkerRefUsersController(
                        "RD-Caseworker-Ref-Api", 20, "caseWorkerId",
                        "preview", caseWorkerServiceImpl, caseWorkerDeleteServiceImpl),
                new StaffReferenceInternalController(
                        "RD-Caseworker-Ref-Api", 20, "caseWorkerId",
                        caseWorkerServiceImpl),
                new StaffRefDataController("RD-Caseworker-Staff-Ref-Api",
                        staffRefDataServiceImpl)
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
        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerId("cwId");
        caseWorkerWorkArea.setServiceCode("BFA1");
        caseWorkerWorkArea.setCaseWorkerProfile(caseWorkerProfile);
        caseWorkerProfile.setCaseWorkerWorkAreas(List.of(caseWorkerWorkArea));
        PageImpl<CaseWorkerProfile> page = new PageImpl<>(List.of(caseWorkerProfile));
        doReturn(page).when(caseWorkerProfileRepo).findByServiceCodeIn(anySet(), any());
    }

    @State({"A list of staff ref data Service skills"})
    public void fetchListOfServiceSkills() throws JsonProcessingException {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);

    }

    private  List<Skill> getSkillsData() {
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1L);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        Skill skill2 = new Skill();
        skill2.setServiceId("BBA3");
        skill2.setSkillId(3L);
        skill2.setSkillCode("A3");
        skill2.setDescription("desc3");
        skill2.setUserType("user_type3");


        Skill skill3 = new Skill();
        skill3.setServiceId("ABA1");
        skill3.setSkillId(2L);
        skill3.setSkillCode("A2");
        skill3.setDescription("desc2");
        skill3.setUserType("user_type2");

        Skill skill4 = new Skill();
        skill4.setServiceId("ABA1");
        skill4.setSkillId(4L);
        skill4.setSkillCode("A4");
        skill4.setDescription("desc4");
        skill4.setUserType("user_type4");

        List<Skill> skills = List.of(skill1,skill2,skill3,skill4);
        return  skills;
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

        List<CaseWorkerSkill> caseWorkerSkills =
                Collections.singletonList(new CaseWorkerSkill(caseWorkerId, 1L));

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
                new UserType(1L, "HMCTS"), false,false, caseWorkerSkills);
    }

    @State({"A list of all staff reference data user-type"})
    public void fetchUserTypes() {
        List<UserType> userTypeList = new ArrayList<>();
        userTypeList.add(new UserType(1L, "User Type 1"));
        userTypeList.add(new UserType(2L, "User Type 2"));
        userTypeList.add(new UserType(3L, "User Type 3"));
        when(userTypeRepository.findAll())
                .thenReturn(userTypeList);
    }

    @State({"A list of all staff reference data role-type"})
    public void retrieveJobTitles() throws JsonProcessingException {
        List<RoleType> roleTypeList = new ArrayList<>();
        roleTypeList.add(new RoleType(1L, "Role Description 1"));
        roleTypeList.add(new RoleType(2L, "Role Description 2"));
        roleTypeList.add(new RoleType(3L, "Role Description 3"));
        when(roleTypeRepository.findAll())
                .thenReturn(roleTypeList);
    }

    @State({"A Staff profile create request is submitted"})
    public void createStaffUserProfile() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        UserProfileCreationResponse userProfileResponse = new UserProfileCreationResponse();
        userProfileResponse.setIdamId(UUID.randomUUID().toString());
        userProfileResponse.setIdamRegistrationResponse(200);
        String body = mapper.writeValueAsString(userProfileResponse);

        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class),anyString());
        doReturn(getCaseWorkerProfile("123456")).when(caseWorkerProfileRepo).save(any());
    }
}


