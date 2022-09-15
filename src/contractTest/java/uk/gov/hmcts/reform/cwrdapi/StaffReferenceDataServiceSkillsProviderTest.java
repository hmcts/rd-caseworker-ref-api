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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.CaseWorkerRefUsersController;
import uk.gov.hmcts.reform.cwrdapi.controllers.StaffRefDataController;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.StaffReferenceInternalController;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.LrdOrgInfoServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerDeleteServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.StaffRefDataServiceImpl;

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
@Provider("staff_referenceData_service_skills")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}"
        , consumerVersionSelectors = {
        @VersionSelector(tag = "Dev")})
//@Import(CaseWorkerProviderTestConfiguration.class)
//@SpringBootTest(properties = {"crd.publisher.caseWorkerDataPerMessage=1"})
@ContextConfiguration(classes = {
        CaseWorkerServiceImpl.class, CaseWorkerDeleteServiceImpl.class,
        StaffRefDataServiceImpl.class
})

@IgnoreNoPactsToVerify
public class StaffReferenceDataProviderServiceSkillsTest {

    @MockBean
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @MockBean
    private CaseWorkerDeleteServiceImpl caseWorkerDeleteServiceImpl;

    @MockBean
    private CaseWorkerProfileRepository caseWorkerProfileRepo;

    @MockBean
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @MockBean
    private LocationReferenceDataFeignClient locationReferenceDataFeignClient;



    @Mock
    private CaseWorkerServiceFacade caseWorkerServiceFacade;

    private static final String USER_ID = "234873";
    private static final String USER_ID2 = "234879";


    @MockBean
    private SkillRepository skillRepository;

    @MockBean
    private StaffRefDataServiceImpl staffRefDataServiceImpl;

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

                new StaffRefDataController("RD-Caseworker-Ref-Api",staffRefDataServiceImpl)
        );
        if (context != null) {
            context.setTarget(testTarget);
        }
    }



    @State({"A list of staff ref data Service skills"})
    public void fetchListOfServiceSkills() throws JsonProcessingException {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

    }

    private  List<Skill> getSkillsData(){
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1l);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        Skill skill2 = new Skill();
        skill2.setServiceId("BBA3");
        skill2.setSkillId(3l);
        skill2.setSkillCode("A3");
        skill2.setDescription("desc3");
        skill2.setUserType("user_type3");


        Skill skill3 = new Skill();
        skill3.setServiceId("ABA1");
        skill3.setSkillId(2l);
        skill3.setSkillCode("A2");
        skill3.setDescription("desc2");
        skill3.setUserType("user_type2");

        Skill skill4 = new Skill();
        skill4.setServiceId("ABA1");
        skill4.setSkillId(4l);
        skill4.setSkillCode("A4");
        skill4.setDescription("desc4");
        skill4.setUserType("user_type4");

        List<Skill> skills = List.of(skill1,skill2,skill3,skill4);
        return  skills;
    }



}


