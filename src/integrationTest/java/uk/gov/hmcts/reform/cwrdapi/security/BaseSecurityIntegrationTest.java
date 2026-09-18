package uk.gov.hmcts.reform.cwrdapi.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.config.TestApplicationServer;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.wiremock.WireMockTestEnvironment;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient.getHttpHeaders;
import static uk.gov.hmcts.reform.cwrdapi.wiremock.IdamWireMockStubs.stubIdamWithGivenRoleAndStatus;

public class BaseSecurityIntegrationTest extends AuthorizationEnabledIntegrationTest {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";
    protected static final String CREATE_STAFF_URI = "/refdata/case-worker/profile";

    static WireMockServer idamMockServer = WireMockTestEnvironment.idam();

    private static StubMapping staffAdminUserStub = null;
    private StaffProfileCreationRequest staffProfileCreationRequest = null;

    @BeforeAll
    public static void setUp() {
        staffAdminUserStub = stubIdamWithGivenRoleAndStatus(idamMockServer, "active", List.of(ROLE_STAFF_ADMIN));
    }

    @BeforeEach
    public void setUP() {
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
    }

    @AfterAll
    public static void cleanUp() {
        idamMockServer.removeStub(staffAdminUserStub);
    }

    @Autowired
    private TestApplicationServer testApplicationServer;

    protected String getStaffProfileCreationRequest() throws JsonProcessingException {

        if (Objects.isNull(staffProfileCreationRequest)) {
            staffProfileCreationRequest = caseworkerReferenceDataClient.createStaffProfileCreationRequest();
        }

        return getObjectMapper().writeValueAsString(staffProfileCreationRequest);
    }

    protected RequestSpecification jwtRequest(
            String issuer,
            boolean expired)
            throws Exception {

        return SerenityRest.given()
                .baseUri(testApplicationServer.getBaseUrl())
                .headers(getHttpHeaders(issuer, expired));

    }

    protected RequestSpecification unexpiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, true);
    }
}
