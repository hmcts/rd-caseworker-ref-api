package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
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

            skills = skillRepository.findAll();
            Optional<List<Skill>> skillsOptional = Optional.ofNullable(skills);
            if(skillsOptional.isPresent()){
                serviceSkills = mapSkillToServicesSkill(skills);
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

    public  List<ServiceSkill> mapSkillToServicesSkill(List<Skill> skills){
        //List<Skill> skills = getSkillsData();

        Map<String, List<Skill>> result = skills.stream()
                .collect(
                        Collectors.toMap(
                                skill -> skill.getServiceId(),
                                skill -> Collections.singletonList(skill),
                                this::mergeSkillsWithDuplicateServiceIds
                        )
                );

        //System.out.println(result);

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
    private  List<Skill> mergeSkillsWithDuplicateServiceIds(List<Skill> existingResults, List<Skill> newResults) {
        List<Skill> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }

}
