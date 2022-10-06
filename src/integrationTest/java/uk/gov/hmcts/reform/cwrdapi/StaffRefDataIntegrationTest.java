package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;


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

        String role = "staff-admin";


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
    void should_return_status_code_400_when_page_size_is_zero()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "cwr-test";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,0,20,role);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_SIZE + " is invalid");

    }

    @Test
    void should_return_status_code_400_when_page_num_is_zero()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "cwr-test";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,1,0,role);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_NUMBER + " is invalid");

    }

    @Test
    void should_return_status_code_400_when_search_String_is_not_valid()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "1234";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,0,20,role);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Invalid search string. Please input a valid string.");

    }

    @Test
    void should_return_status_code_400_when_search_String_len_less_3()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "ab";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,0,20,role);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("The search string should contain at least 3 characters.");

    }

    @Test
    void should_return_status_code_400_when_search_String_empty()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String role = "cwd-admin";
        String searchString = "";

        Map<String, Object> response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,0,20,role);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Required request parameter 'search' for method parameter type String is not present");

    }

}
