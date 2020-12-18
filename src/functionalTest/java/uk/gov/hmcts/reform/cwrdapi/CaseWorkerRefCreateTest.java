package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Test
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
    }

    @Test
    public void whenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponse.getRoles());
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidam_Ac3() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        CaseWorkersProfileCreationRequest request = profileCreateRequests.get(0);
        Set<String> idamRole = request.getIdamRoles();
        idamRole.add(CASEWORKER_IAC);
        idamRole.add(CASEWORKER_IAC_BULKSCAN);
        request.setIdamRoles(idamRole);

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CASEWORKER_IAC,CWD_USER,CASEWORKER_IAC_BULKSCAN),
                upResponseForExistingUser.getRoles());
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndDeleteFlagTrue_Ac4() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        profileCreateRequests.get(0).setDeleteFlag(true);
        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(upResponseForExistingUser.getIdamStatus(), USER_STATUS_SUSPENDED);
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndRolesAreSame_Ac5() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponseForExistingUser.getRoles());
    }

    public List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile() {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(userEmail);

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        return caseWorkersProfileCreationRequests;
    }

    public UserProfileResponse getUserProfileFromUp(String email) {
        return funcTestRequestHandler.sendGet(OK,
                "/v1/userprofile/roles?email=" + email, UserProfileResponse.class, baseUrlUserProfile);
    }
}