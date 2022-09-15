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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.StaffRefDataController;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.service.impl.StaffRefDataServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("staff_referenceData_service_skills")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors = {
        @VersionSelector(tag = "master")})

@IgnoreNoPactsToVerify
public class StaffReferenceDataServiceSkillsProviderTest {

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
        StaffWorkerSkillResponse staffWorkerSkillResponse = getServiceSkills();
        when(staffRefDataServiceImpl.getServiceSkills()).thenReturn(staffWorkerSkillResponse);

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


    public StaffWorkerSkillResponse getServiceSkills() {
        List<Skill> skills = getSkillsData();
        List<ServiceSkill> serviceSkills = new ArrayList<>();

            List<SkillDTO> skillData = new ArrayList<>();
            //skills = skillRepository.findAll();
            Optional<List<Skill>> skillsOptional = Optional.ofNullable(skills);
            if(skillsOptional.isPresent()){
                skillData = skills.stream().map(skill -> {
                    SkillDTO skillDTO = new SkillDTO();
                    skillDTO.setServiceId(skill.getServiceId());
                    skillDTO.setSkillId(skill.getSkillId());
                    skillDTO.setSkillCode(skill.getSkillCode());
                    skillDTO.setUserType(skill.getUserType());
                    skillDTO.setDescription(skill.getDescription());
                    return skillDTO;
                }).toList();

                serviceSkills = mapSkillToServicesSkill(skillData);
            }


        StaffWorkerSkillResponse staffWorkerSkillResponse = new StaffWorkerSkillResponse();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        return staffWorkerSkillResponse;
    }

    public  List<ServiceSkill> mapSkillToServicesSkill(List<SkillDTO> skillData ){
        //List<Skill> skills = getSkillsData();

        Map<String, List<SkillDTO>> result = skillData.stream()
                .collect(
                        Collectors.toMap(
                                skill -> skill.getServiceId(),
                                skill -> Collections.singletonList(skill),
                                this::mergeSkillsWithDuplicateServiceIds
                        )
                );


        List<ServiceSkill> serviceSkills = new ArrayList<>();

        result.forEach(
                (key,value)->{
                    serviceSkills.add(
                            ServiceSkill.builder()
                                    .id(key)
                                    .skills(value).build());
                }
        );
        return serviceSkills;

    }
    private  List<SkillDTO> mergeSkillsWithDuplicateServiceIds(List<SkillDTO> existingResults, List<SkillDTO> newResults) {
        List<SkillDTO> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }
}


