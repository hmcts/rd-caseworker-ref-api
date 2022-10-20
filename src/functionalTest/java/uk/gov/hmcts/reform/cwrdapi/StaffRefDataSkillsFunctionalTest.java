package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

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
     void should_return_service_skills_with_status_code_200_when_flag_true() {

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
        assertThat(staffWorkerSkillResponse.getServiceSkills().size()).isEqualTo(5);

        ServiceSkill serviceSkill = staffWorkerSkillResponse.getServiceSkills().get(0);
        assertThat(serviceSkill.getId()).isEqualTo("1");
        assertThat(serviceSkill.getSkills().size()).isEqualTo(1);

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
