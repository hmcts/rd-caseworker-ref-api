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

        assertThat(serviceSkills.size()).isEqualTo(3);

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("AAA7");

        assertThat(serviceSkill.getSkills().size()).isEqualTo(4);

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(9L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("SKILL:AAA7:TEST1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("CTSC");
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
        assertThat(serviceSkills.size()).isEqualTo(3);

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
        String path = "/job-title";

        String role = "staff-admin";


        final var staffRefJobTitleResponse = (StaffRefJobTitleResponse) caseworkerReferenceDataClient
                .retrieveStaffRefData(StaffRefJobTitleResponse.class, path, role);

        assertThat(staffRefJobTitleResponse).isNotNull();

        List<StaffRefDataJobTitle> jobTitles = staffRefJobTitleResponse.getJobTitles();

        assertThat(jobTitles).isNotNull();
        assertThat(jobTitles).hasSize(18);

        StaffRefDataJobTitle staffRefDataJobTitle = jobTitles.get(0);

        assertThat(staffRefDataJobTitle.getRoleId()).isEqualTo(3L);
        assertThat(staffRefDataJobTitle.getRoleDescription()).isEqualTo("Hearing Centre Team Leader");

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
