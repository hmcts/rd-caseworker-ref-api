package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
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
import uk.gov.hmcts.reform.cwrdapi.client.CaseWorkerApiClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.client.response.S2sClient;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffRefJobTitleFunctionalTest extends AuthorizationFunctionalTest {
    public static final String mapKey = "StaffRefDataController.retrieveJobTitles";

    protected static CaseWorkerApiClient caseWorkerApiClient;

    @PostConstruct
    public void beforeTestClass() {

        SerenityRest.useRelaxedHTTPSValidation();

        if (null == s2sToken) {
            log.info(":::: Generating S2S Token");
            s2sToken = new S2sClient(
                    testConfigProperties.getS2sUrl(),
                    testConfigProperties.getS2sName(),
                    testConfigProperties.getS2sSecret())
                    .signIntoS2S();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(testConfigProperties);
        }

        caseWorkerApiClient = new CaseWorkerApiClient(
                caseWorkerApiUrl,
                s2sToken, idamOpenIdClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    void fetchJobTitlesAllStatusCode_200() {
        StaffRefJobTitleResponse responses = (StaffRefJobTitleResponse)
                caseWorkerApiClient.fetchJobTitles(
                        HttpStatus.OK
                );
        assertTrue(responses.getJobTitles().size() > 0);
    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = mapKey, withFeature = false)
    void should_fetchJobTitles_403_when_Api_toggled_off() {

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_STAFF_ADMIN)
                .get("/refdata/case-worker/job-title")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());
    }

}
