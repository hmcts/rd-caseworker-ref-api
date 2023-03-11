package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FetchStaffProfileByIdFunctionalTest extends AuthorizationFunctionalTest {

    public static final String FETCH_STAFF_PROFILE_BY_ID = "StaffRefDataController.fetchStaffProfileById";
    public static final String STAFF_PROFILE_URL = "/refdata/case-worker";

    @Test
    // @ToggleEnable(mapKey = FETCH_STAFF_PROFILE_BY_ID, withFeature = true)
    //@ExtendWith(FeatureToggleConditionExtension.class)
    void should_fetchStaffProfile_By_ID_200() {
        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(9)
                .description("testskill1")
                .skillCode("SKILL:AAA7:TEST1")
                .build();

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        staffProfileCreationRequest.setSkills(List.of(skillsRequest));

        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        assertNotNull(response);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);
        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());

        String firstCaseworkerId = staffProfileCreationResponse.getCaseWorkerId();

        IdamOpenIdClient.cwdStaffAdminUserToken = null;
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_STAFF_ADMIN)
                .get(STAFF_PROFILE_URL + "/profile/search-by-id?id=" + firstCaseworkerId)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        SearchStaffUserByIdResponse caseWorkerProfile =
                fetchResponse.getBody().as(SearchStaffUserByIdResponse.class);

        assertThat(caseWorkerProfile).isNotNull();

        assertEquals(firstCaseworkerId, caseWorkerProfile.getCaseWorkerId());
        assertEquals(staffProfileCreationRequest.getFirstName(), caseWorkerProfile.getFirstName());
        assertEquals(staffProfileCreationRequest.getLastName(), caseWorkerProfile.getLastName());
        assertEquals(staffProfileCreationRequest.getEmailId(), caseWorkerProfile.getEmailId());
        assertEquals(staffProfileCreationRequest.isStaffAdmin(), caseWorkerProfile.isStaffAdmin());
        assertEquals(staffProfileCreationRequest.getBaseLocations().size(),
                caseWorkerProfile.getBaseLocations().size());
        assertEquals(staffProfileCreationRequest.getBaseLocations().get(0).getLocation(),
                caseWorkerProfile.getBaseLocations().get(0).getLocationName());
        assertEquals(staffProfileCreationRequest.getServices().size(), caseWorkerProfile.getServices().size());
        assertEquals(staffProfileCreationRequest.getServices().get(0).getService(),
                caseWorkerProfile.getServices().get(0).getService());
        assertEquals(staffProfileCreationRequest.getRoles().size(), caseWorkerProfile.getRoles().size());
        assertEquals(staffProfileCreationRequest.getRoles().get(0).getRole(),
                caseWorkerProfile.getRoles().get(0).getRoleName());
        assertThat(caseWorkerProfile.getSkills().size()).isGreaterThanOrEqualTo(1);
        assertEquals(staffProfileCreationRequest.getSkills().get(0).getSkillId(),
                caseWorkerProfile.getSkills().get(0).getSkillId());
        assertEquals(staffProfileCreationRequest.getSkills().get(0).getDescription(),
                caseWorkerProfile.getSkills().get(0).getDescription());
    }
}
