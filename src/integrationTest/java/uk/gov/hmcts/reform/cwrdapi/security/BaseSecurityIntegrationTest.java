package uk.gov.hmcts.reform.cwrdapi.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.config.TestApplicationServer;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient.getHttpHeaders;

public class BaseSecurityIntegrationTest extends AuthorizationEnabledIntegrationTest {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";
    protected static final String CREATE_STAFF_URI = "/refdata/case-worker/profile";

    private static final String USER_ID = UUID.randomUUID().toString();
    private StaffProfileCreationRequest staffProfileCreationRequest = null;

    @Autowired
    private TestApplicationServer testApplicationServer;

    @BeforeEach
    public void setUP() {
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
    }

    protected String getStaffProfileCreationRequest() throws JsonProcessingException {

        if (Objects.isNull(staffProfileCreationRequest)) {
            staffProfileCreationRequest = caseworkerReferenceDataClient.createStaffProfileCreationRequest();
        }

        return getObjectMapper().writeValueAsString(staffProfileCreationRequest);
    }

    protected RequestSpecification jwtRequest(String issuer, boolean expired) {

        return SerenityRest.given()
                .baseUri(testApplicationServer.getBaseUrl())
                .headers(getHttpHeaders(issuer, expired, USER_ID, ROLE_STAFF_ADMIN));

    }

    protected RequestSpecification unexpiredJwt(String issuer) {
        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(String issuer) {
        return jwtRequest(issuer, true);
    }
}
