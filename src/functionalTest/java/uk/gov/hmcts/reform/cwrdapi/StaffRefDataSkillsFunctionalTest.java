package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StaffRefDataSkillsFunctionalTest extends AuthorizationFunctionalTest {
    public static final String STAFF_REF_DATA_SKILL_URL = "/refdata/case-worker/skill";
    public static final String STAFF_REF_DATA_RD_STAFF_UI_KEY =
            "StaffRefDataController.retrieveAllServiceSkills";


    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
     void should_return_service_skills_with_status_code_200_when_flag_true() throws JsonProcessingException {

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        StaffWorkerSkillResponse staffWorkerSkillResponse =
                fetchResponse.getBody().as(StaffWorkerSkillResponse.class);
        assertThat(staffWorkerSkillResponse).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills()).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills().size()).isGreaterThan(0);

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills.size()).isEqualTo(4);
        for (ServiceSkill ss: serviceSkills) {
            List<SkillDTO> listOfSkillsForSsid = fetchListOfSkills(ss.getId());
            assertThat(ss.getSkills().size()).isEqualTo(listOfSkillsForSsid.size());
            for (int i = 0; i < ss.getSkills().size(); i++) {
                SkillDTO skillDto = ss.getSkills().get(i);
                assertThat(skillDto.getSkillId()).isEqualTo(listOfSkillsForSsid.get(i).getSkillId());
                assertThat(skillDto.getSkillCode()).isEqualTo(listOfSkillsForSsid.get(i).getSkillCode());
                assertThat(skillDto.getDescription()).isEqualTo(listOfSkillsForSsid.get(i).getDescription());
                assertThat(skillDto.getUserType()).isEqualTo(listOfSkillsForSsid.get(i).getUserType());
            }
        }

    }

    List<SkillDTO> fetchListOfSkills(String code) throws JsonProcessingException {
        Response staffWorkerSkillResponse = caseWorkerApiClient
            .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
            .get(STAFF_REF_DATA_SKILL_URL  + "?service_codes=" + code)
            .andReturn();
        staffWorkerSkillResponse.then()
            .assertThat()
            .statusCode(200);
        StaffWorkerSkillResponse swResponse =
            staffWorkerSkillResponse.getBody().as(StaffWorkerSkillResponse.class);
        assertThat(staffWorkerSkillResponse).isNotNull();
        List<ServiceSkill> serviceSkills = swResponse.getServiceSkills();
        assertThat(swResponse.getServiceSkills().size()).isEqualTo(1);

        return serviceSkills.get(0).getSkills();
    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_service_skills_with_status_code_403_when_flag_false() {

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(fetchResponse.statusCode());
        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_401_when_authentication_invalid() {
        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_403_when_invalid_role() {
        Response response = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);
    }

}
