package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
    }

    @BeforeAll
    public static void setup() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @AfterAll
    public static void tearDown() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    void should_retrieveAllServiceSkills_return_status_code_200()
            throws JsonProcessingException {
        String path = "/skill";
        String role = "cwd-admin";

        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("1");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(1L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("1");
    }

    @Test
    void should_return_status_code_200_and_list_of_staff_users_by_name()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "cwr-test";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,20,1,role);

        assertThat(response).containsEntry("http_status", "200");
       /* assertTrue(response.get("response_body").toString()
                .contains("Required request parameter 'ccd_service_names'"
                        + " for method parameter type String is not present"));*/

    }

    @Test
    public void shouldReturn400ForEmptyServiceName() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("", null, null,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains("Required request parameter 'ccd_service_names'"
                        + " for method parameter type String is not present"));
    }
}
