package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;

import net.serenitybdd.junit5.SerenityTest;
import net.serenitybdd.junit5.extension.SerenityExtension;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SerenityTest
@ExtendWith(SerenityExtension.class)
@WithTags({@WithTag("testType:Smoke")})
public class SmokeTest {

    // use this when testing locally - replace the below content with this line
    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8095"
        );

    @Test
    public void test_should_prove_app_is_running_and_healthy() {
        // local test
        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        SerenityRest.useRelaxedHTTPSValidation();


        Response response = SerenityRest
                .given().log().all()
                .baseUri(targetInstance)
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();

        if (null != response && response.statusCode() == 200) {
            assertThat(response.body().asString())
                .contains("Message for the Caseworker Ref Data API");

        } else {
            Assertions.fail();
        }
    }
}
