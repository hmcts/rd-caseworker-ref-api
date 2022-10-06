package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    SkillRepository skillRepository;


    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Override
    public List<UserType> fetchUserTypes() {
        return userTypeRepository
                .findAll();
    }



    @Override
    public StaffWorkerSkillResponse getServiceSkills() {
        List<Skill> skills = null;
        List<ServiceSkill> serviceSkills = null;
        try {
            List<SkillDTO> skillData = null;
            skills = skillRepository.findAll();

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

        } catch (Exception exp) {
            log.error("{}:: StaffRefDataService getServiceSkills failed :: {}", loggingComponentName,
                    exp);
            throw exp;
        }
        StaffWorkerSkillResponse staffWorkerSkillResponse = new StaffWorkerSkillResponse();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        return staffWorkerSkillResponse;
    }

    /**
     * To convert skills data to ServiceSkills.
     * @param skillData List of skills
     * @return List of ServiceSkill
     */
    public List<ServiceSkill> mapSkillToServicesSkill(List<SkillDTO> skillData) {
        Map<String, List<SkillDTO>> result = null;
        if (skillData != null) {
            result = skillData.stream()
                    .collect(
                            Collectors.toMap(
                                    SkillDTO::getServiceId,
                                    Collections::singletonList,
                                    this::mergeSkillsWithDuplicateServiceIds
                            )
                    );
        }


        List<ServiceSkill> serviceSkills = new ArrayList<>();

        if (result != null) {
            result.forEach(
                    (key, value) -> {
                        ServiceSkill serviceSkill = new ServiceSkill();
                        serviceSkill.setId(key);
                        serviceSkill.setSkills(value);
                        serviceSkills.add(serviceSkill);
                    }
            );
        }

        return serviceSkills;

    }

    private List<SkillDTO> mergeSkillsWithDuplicateServiceIds(List<SkillDTO> existingResults,
                                                              List<SkillDTO> newResults) {
        List<SkillDTO> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }


    @Override
    public List<RoleType> getJobTitles() {
        return roleTypeRepository.findAll();
    }

    @Override
    public ResponseEntity<Object> retrieveStaffProfile(SearchRequest searchRequest, Object pageRequest) {
        return null;
    }



}
