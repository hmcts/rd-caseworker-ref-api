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

        assertThat(serviceSkills).isNotNull();

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("1");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(1L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("1");
        assertThat(skillDTO.getDescription()).isEqualTo("testskill1");
        assertThat(skillDTO.getUserType()).isEqualTo("1");
    }
}
