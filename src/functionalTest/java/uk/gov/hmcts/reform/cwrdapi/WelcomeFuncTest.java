package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SerenityTest
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class WelcomeFuncTest {

    // use this when testing locally - replace the below content with this line
    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8095"
        );

    @Test
    public void test_should_prove_app_is_running_and_healthy() {
        // local test
        SerenityRest.useRelaxedHTTPSValidation();

        Response response = SerenityRest
                .given().log().all()
                .baseUri(targetInstance)
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();
        if (null != response && response.statusCode() == 200) {
            assertThat(response.body().asString()).isNotNull();

        } else {

            Assertions.fail();
        }
    }
}
