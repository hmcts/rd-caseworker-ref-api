package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

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

        assertThat(serviceSkills.size()).isEqualTo(4);

        for (ServiceSkill ss: serviceSkills) {
            List<SkillDTO> listOfSkillsForSSId = fetchListOfSkills(ss.getId());
            assertThat(ss.getSkills().size()).isEqualTo(listOfSkillsForSSId.size());
            for (int i = 0; i < ss.getSkills().size(); i++) {
                SkillDTO skillDTO = ss.getSkills().get(i);
                assertThat(skillDTO.getSkillId()).isEqualTo(listOfSkillsForSSId.get(i).getSkillId());
                assertThat(skillDTO.getSkillCode()).isEqualTo(listOfSkillsForSSId.get(i).getSkillCode());
                assertThat(skillDTO.getDescription()).isEqualTo(listOfSkillsForSSId.get(i).getDescription());
                assertThat(skillDTO.getUserType()).isEqualTo(listOfSkillsForSSId.get(i).getUserType());
            }
        }

    }

    List<SkillDTO> fetchListOfSkills(String code) throws JsonProcessingException {
        String path = "/skill?service_codes=" + code;
        String role = "staff-admin";
        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
            .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);
        assertThat(staffWorkerSkillResponse).isNotNull();
        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills.size()).isEqualTo(1);
        return serviceSkills.get(0).getSkills();
    }


    @Test
    void should_retrieveAllServiceSkills_return_status_code_200_when_empty_servicecode()
            throws JsonProcessingException {
        String path = "/skill?service_codes=";

        String role = "staff-admin";


        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();
        assertThat(serviceSkills.size()).isEqualTo(4);

        ServiceSkill serviceSkill = serviceSkills.get(1);

        assertThat(serviceSkill.getId()).isEqualTo("AAA7");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(serviceSkill.getSkills().size()).isEqualTo(4);

        assertThat(skillDTO.getSkillId()).isEqualTo(9L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("SKILL:AAA7:TEST1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("CTSC");
    }

    @Test
    void should_retrieveAllServiceSkills_return_status_code_200_when_provide_multiple_servicecode()
            throws JsonProcessingException {
        String path = "/skill?service_codes=ABA5,AAA7";

        String role = "staff-admin";


        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills.size()).isEqualTo(2);

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("AAA7");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(serviceSkill.getSkills().size()).isEqualTo(4);

        assertThat(skillDTO.getSkillId()).isEqualTo(9L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("SKILL:AAA7:TEST1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("CTSC");


        serviceSkill = serviceSkills.get(1);

        assertThat(serviceSkill.getId()).isEqualTo("ABA5");

        skillDTO = serviceSkill.getSkills().get(0);

        assertThat(serviceSkill.getSkills().size()).isEqualTo(11);


        assertThat(skillDTO.getSkillId()).isEqualTo(26L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("SKILL:ABA5:CHECKAPPLICATIONC100");
        assertThat(skillDTO.getDescription()).isEqualTo("Check application C100");
        assertThat(skillDTO.getUserType()).isEqualTo("CTSC");

    }

    @Test
    void should_retrieveAllServiceSkills_return_status_code_200_when_provide_single_servicecode()
            throws JsonProcessingException {
        String path = "/skill?service_codes=AAA7";

        String role = "staff-admin";


        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills.size()).isEqualTo(1);

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("AAA7");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(serviceSkill.getSkills().size()).isEqualTo(4);

        assertThat(skillDTO.getSkillId()).isEqualTo(9L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("SKILL:AAA7:TEST1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("CTSC");


    }

    @Test
    void should_retrieveAllServiceSkills_return_status_code_200_when_provide_invalid_servicecode()
            throws JsonProcessingException {
        String path = "/skill?service_codes=Invalid";

        String role = "staff-admin";


        final var staffWorkerSkillResponse = (StaffWorkerSkillResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffWorkerSkillResponse.class, path, role);

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills.size()).isEqualTo(0);

    }

    @Test
    void should_retrieveAllTitles_return_status_code_200()
            throws JsonProcessingException {

        final var staffRefJobTitleResponse =
                (StaffRefJobTitleResponse) caseworkerReferenceDataClient.retrieveStaffRefData(
                        StaffRefJobTitleResponse.class,
                        "/job-title",
                        CaseWorkerReferenceDataClient.ROLE_STAFF_ADMIN);

        assertThat(staffRefJobTitleResponse).isNotNull();

        List<StaffRefDataJobTitle> jobTitles = staffRefJobTitleResponse.getJobTitles();

        assertThat(jobTitles).isNotNull();
        assertThat(jobTitles).hasSize(25);
        org.hamcrest.MatcherAssert.assertThat(jobTitles, containsInAnyOrder(
                new StaffRefDataJobTitle(1L, "Senior Legal Caseworker"),
                new StaffRefDataJobTitle(2L, "Legal Caseworker"),
                new StaffRefDataJobTitle(3L, "Hearing Centre Team Leader"),
                new StaffRefDataJobTitle(4L, "Hearing Centre Administrator"),
                new StaffRefDataJobTitle(5L, "Court Clerk"),
                new StaffRefDataJobTitle(6L, "National Business Centre Team Leader"),
                new StaffRefDataJobTitle(7L, "National Business Centre Listing Team"),
                new StaffRefDataJobTitle(8L, "National Business Centre Payments Team"),
                new StaffRefDataJobTitle(9L, "CTSC Team Leader"),
                new StaffRefDataJobTitle(10L, "CTSC Administrator"),
                new StaffRefDataJobTitle(11L, "National Business Centre Administrator"),
                new StaffRefDataJobTitle(12L, "Regional Centre Team Leader"),
                new StaffRefDataJobTitle(13L, "Regional Centre Administrator"),
                new StaffRefDataJobTitle(14L, "DWP Caseworker"),
                new StaffRefDataJobTitle(15L, "HMRC Caseworker"),
                new StaffRefDataJobTitle(16L, "Registrar"),
                new StaffRefDataJobTitle(17L, "CICA Caseworker"),
                new StaffRefDataJobTitle(18L, "Cafcass Cymru Caseworker"),
                new StaffRefDataJobTitle(19L, "IBCA Caseworker"),
                new StaffRefDataJobTitle(20L, "WLU Administrator"),
                new StaffRefDataJobTitle(21L, "WLU Team Leader"),
                new StaffRefDataJobTitle(22L, "HRS Team Leader"),
                new StaffRefDataJobTitle(23L, "Bailiff Administrator"),
                new StaffRefDataJobTitle(24L, "Bailiff"),
                new StaffRefDataJobTitle(25L, "Bailiff Manager")));
    }

    @Test
    void should_retrieveAllUserTypes_return_status_code_200()
            throws JsonProcessingException, JSONException {
        String path = "/user-type";

        String role = "staff-admin";


        final var staffRefDataUserTypesResponse = (StaffRefDataUserTypesResponse) caseworkerReferenceDataClient
                .retrieveStaffRefData(StaffRefDataUserTypesResponse.class, path, role);

        assertThat(staffRefDataUserTypesResponse).isNotNull();

        List<StaffRefDataUserType> userTypes = staffRefDataUserTypesResponse.getUserTypes();

        assertThat(userTypes).isNotNull();
        assertThat(userTypes).hasSize(5);


        validateUserTypes(userTypes);

    }

    void validateUserTypes(List<StaffRefDataUserType> userTypes) throws JsonProcessingException, JSONException {

        ObjectMapper mapper = new ObjectMapper();

        String actual = mapper.writeValueAsString(getUserTypesData());
        String userTypesData = mapper.writeValueAsString(userTypes);

        JSONAssert.assertEquals(userTypesData, actual, JSONCompareMode.LENIENT);

    }

    private List<StaffRefDataUserType> getUserTypesData() {

        List<StaffRefDataUserType> userTypes = new ArrayList<>();
        StaffRefDataUserType staffRefDataUserType;

        userTypes.add(StaffRefDataUserType.builder()
                .id(1L)
                .code("CTSC")
                .build());

        userTypes.add(StaffRefDataUserType.builder()
                .id(2L)
                .code("Future Operations")
                .build());

        userTypes.add(StaffRefDataUserType.builder()
                .id(3L)
                .code("Legal office")
                .build());

        userTypes.add(StaffRefDataUserType.builder()
                .id(4L)
                .code("NBC")
                .build());

        userTypes.add(StaffRefDataUserType.builder()
                .id(5L)
                .code("Other Government Department")
                .build());


        return userTypes;
    }
}
