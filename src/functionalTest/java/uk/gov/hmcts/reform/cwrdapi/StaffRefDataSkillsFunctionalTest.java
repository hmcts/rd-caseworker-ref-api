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
    public static final int SKILL_COUNT = 5;
    public static final int AAA7_SKILL_COUNT = 2;
    public static final int HRS1_SKILL_COUNT = 15;
    public static final int BHA1_SKILL_COUNT = 1;
    public static final int AAA6_SKILL_COUNT = 2;
    public static final int ABA5_SKILL_COUNT = 13;


    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
     void should_return_service_skills_with_status_code_200_when_flag_true() throws JsonProcessingException {

        StaffWorkerSkillResponse staffWorkerSkillResponse = fetchResponse(null);
        assertThat(staffWorkerSkillResponse).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills()).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills.size()).isEqualTo(SKILL_COUNT);

        for (ServiceSkill ss: serviceSkills) {
            log.info(":::: skill fetched is" + ss.getId());
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
        StaffWorkerSkillResponse swResponse = fetchResponse(code);
        assertThat(swResponse).isNotNull();
        List<ServiceSkill> serviceSkills = swResponse.getServiceSkills();
        assertThat(swResponse.getServiceSkills().size()).isEqualTo(1);
        return serviceSkills.get(0).getSkills();
    }

    StaffWorkerSkillResponse fetchResponse(String code) throws JsonProcessingException {
        String url = (code != null && !code.trim().isEmpty())
            ? STAFF_REF_DATA_SKILL_URL + "?service_codes=" + code
            : STAFF_REF_DATA_SKILL_URL;
        Response fetchResponse = caseWorkerApiClient
            .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
            .get(url)
            .andReturn();
        fetchResponse.then()
            .assertThat()
            .statusCode(200);

        return fetchResponse.getBody().as(StaffWorkerSkillResponse.class);
    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_service_skills_with_status_code_200_verify_heat_count() throws JsonProcessingException {

        StaffWorkerSkillResponse staffWorkerSkillResponse = fetchResponse(null);
        assertThat(staffWorkerSkillResponse).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills()).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills().size()).isEqualTo(SKILL_COUNT);

        for (ServiceSkill serviceSkill: staffWorkerSkillResponse.getServiceSkills()) {
            if (serviceSkill.getId().equalsIgnoreCase("AAA7")) {
                assertThat(serviceSkill.getSkills().size()).isEqualTo(AAA7_SKILL_COUNT);
            } else if (serviceSkill.getId().equalsIgnoreCase("HRS1")) {
                assertThat(serviceSkill.getSkills().size()).isEqualTo(HRS1_SKILL_COUNT);
            } else if (serviceSkill.getId().equalsIgnoreCase("BHA1")) {
                assertThat(serviceSkill.getSkills().size()).isEqualTo(BHA1_SKILL_COUNT);
            } else if (serviceSkill.getId().equalsIgnoreCase("AAA6")) {
                assertThat(serviceSkill.getSkills().size()).isEqualTo(AAA6_SKILL_COUNT);
            } else if (serviceSkill.getId().equalsIgnoreCase("ABA5")) {
                assertThat(serviceSkill.getSkills().size()).isEqualTo(ABA5_SKILL_COUNT);
            }
        }

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
