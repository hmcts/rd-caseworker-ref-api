package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("AbbreviationAsWordInName")
@ExtendWith(MockitoExtension.class)
public class StaffRefDataServiceImplTest {
    @Mock
    private SkillRepository skillRepository;
    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void should_return_non_empty_list_of_service_skills() {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("BBA3");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(1L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("A1");
        assertThat(skillDTO.getDescription()).isEqualTo("desc1");
        assertThat(skillDTO.getUserType()).isEqualTo("user_type1");

    }

    @Test
    void should_return_empty_list_of_service_skills() {
        List<Skill> skills = new ArrayList<>();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills).isNotNull();

        assertThat(serviceSkills.size()).isEqualTo(0);
    }

    @Test
    void should_return_null_list_of_service_skills() {
        List<Skill> skills = null;
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills).isNull();

    }


    private List<Skill> getSkillsData() {
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1L);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        Skill skill2 = new Skill();
        skill2.setServiceId("BBA3");
        skill2.setSkillId(3L);
        skill2.setSkillCode("A3");
        skill2.setDescription("desc3");
        skill2.setUserType("user_type3");


        Skill skill3 = new Skill();
        skill3.setServiceId("ABA1");
        skill3.setSkillId(2L);
        skill3.setSkillCode("A2");
        skill3.setDescription("desc2");
        skill3.setUserType("user_type2");

        Skill skill4 = new Skill();
        skill4.setServiceId("ABA1");
        skill4.setSkillId(4L);
        skill4.setSkillCode("A4");
        skill4.setDescription("desc4");
        skill4.setUserType("user_type4");

        List<Skill> skills = List.of(skill1, skill2, skill3, skill4);
        return skills;
    }
}
