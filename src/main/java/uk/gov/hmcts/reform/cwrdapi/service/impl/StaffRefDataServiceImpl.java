package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    SkillRepository skillRepository;

    @Override
    public StaffWorkerSkillResponse getServiceSkills() {
        List<Skill> skills = null;
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        try{
            List<SkillDTO> skillData = new ArrayList<>();
            skills = skillRepository.findAll();
            Optional<List<Skill>> skillsOptional = Optional.ofNullable(skills);
            if(skillsOptional.isPresent()){
                skillData = skills.stream().map(skill -> {
                    SkillDTO skillDTO = new SkillDTO();
                    skillDTO.setServiceId(skill.getServiceId());
                    skillDTO.setSkillId(skill.getSkillId());
                    skillDTO.setSkillCode(skill.getSkillCode());
                    skillDTO.setUserType(skill.getUserType());
                    skillDTO.setDescription(skill.getDescription());
                    return skillDTO;
                }).toList();

                serviceSkills = mapSkillToServicesSkill(skillData);
            }

              } catch (Exception exp) {
        log.error("{}:: StaffRefDataService getServiceSkills failed :: {}", loggingComponentName,
                 exp);
        throw exp;
    }
        StaffWorkerSkillResponse staffWorkerSkillResponse = new StaffWorkerSkillResponse();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        return staffWorkerSkillResponse;
    }

    public  List<ServiceSkill> mapSkillToServicesSkill(List<SkillDTO> skillData ){
        //List<Skill> skills = getSkillsData();

        Map<String, List<SkillDTO>> result = skillData.stream()
                .collect(
                        Collectors.toMap(
                                skill -> skill.getServiceId(),
                                skill -> Collections.singletonList(skill),
                                this::mergeSkillsWithDuplicateServiceIds
                        )
                );


        List<ServiceSkill> serviceSkills = new ArrayList<>();

        result.forEach(
                (key,value)->{
                    serviceSkills.add(
                            ServiceSkill.builder()
                                    .id(key)
                                    .skills(value).build());
                }
        );
return serviceSkills;

    }
    private  List<SkillDTO> mergeSkillsWithDuplicateServiceIds(List<SkillDTO> existingResults, List<SkillDTO> newResults) {
        List<SkillDTO> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }

}
