package uk.gov.hmcts.reform.cwrdapi.idam;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.lib.idam.IdamOpenId;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_CWD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_CWD_SYSTEM_USER;

@Slf4j
public class IdamOpenIdClient extends IdamOpenId {


    private final Gson gson = new Gson();

    public static String crdAdminToken;

    private static String sidamPassword;

    public static String cwdSystemUserToken;

    public IdamOpenIdClient(TestConfigProperties testConfig) {
        super(testConfig);
    }

    public Map getUser(String idamId) {
        log.info(":::: Get a User");

        Response generatedUserResponse = RestAssured.given().relaxedHTTPSValidation()
            .baseUri(testConfig.getIdamApiUrl())
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .get("/testing-support/accounts/" + idamId)
            .andReturn();
        if (generatedUserResponse.getStatusCode() == 404) {
            log.info("SIDAM getUser response 404");
        }
        return generatedUserResponse.getBody().as(Map.class);
    }

    public String getOpenIdTokenByRole(String role) {
        if (StringUtils.isNotEmpty(role)) {
            if (ROLE_CWD_ADMIN.equals(role)) {
                return getcwdAdminOpenIdToken(role);
            } else if (ROLE_CWD_SYSTEM_USER.equals(role)) {
                return getCwdSystemUserOpenIdToken(role);
            } else {
                return getToken(role);
            }
        } else {
            getToken(ROLE_CWD_ADMIN);
        }
        return null;
    }



    public void deleteSidamUser(String email) {
        try {
            RestAssured
                    .given()
                    .relaxedHTTPSValidation()
                    .baseUri(testConfig.getIdamApiUrl())
                    .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                    .delete("/testing-support/accounts/" + email);
        } catch (Exception ex) {
            log.error("unable to delete sidam user with email");
        }
    }

}