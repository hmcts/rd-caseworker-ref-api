package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
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
@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;
    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;
    @Autowired
    SkillRepository skillRepository;


    @Override
    public ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffUserByName(String searchString,
                                                                                 PageRequest pageRequest) {

        Page<CaseWorkerProfile> pageable =
            caseWorkerProfileRepo.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest);
        long totalRecords = pageable.getTotalElements();

        List<CaseWorkerProfile> caseWorkerProfiles = pageable.getContent();

        List<SearchStaffUserResponse> searchResponse = new ArrayList<>();

        if (!caseWorkerProfiles.isEmpty()) {
            searchResponse = mapCaseWorkerProfilesToSearchResponse(caseWorkerProfiles);
        }

        return ResponseEntity
            .status(200)
            .header("total-records",String.valueOf(totalRecords))
            .body(searchResponse);
    }

    private List<SearchStaffUserResponse> mapCaseWorkerProfilesToSearchResponse(List<CaseWorkerProfile>
                                                                                        caseWorkerProfiles) {
        List<SearchStaffUserResponse> searchStaffUserResponse = new ArrayList<>();
        caseWorkerProfiles.forEach(caseWorkerProfile -> {
            SearchStaffUserResponse searchStaffUserResponseValue =
                    SearchStaffUserResponse.builder()
                            .firstName(caseWorkerProfile.getFirstName())
                            .lastName(caseWorkerProfile.getLastName())
                            .emailId(caseWorkerProfile.getEmailId())
                            .services(mapServicesToDto(caseWorkerProfile.getCaseWorkerWorkAreas()))
                            .region(caseWorkerProfile.getRegion())
                            .regionId(caseWorkerProfile.getRegionId())
                            .roles(mapRolesToDto(caseWorkerProfile.getCaseWorkerRoles()))
                            .baseLocations(mapBaseLocationsToDto(caseWorkerProfile.getCaseWorkerLocations()))
                            .userType(caseWorkerProfile.getUserType().getDescription())
                            .skills(mapSkillsToDto(caseWorkerProfile.getCaseWorkerSkills()))
                            .build();

            if (caseWorkerProfile.getTaskSupervisor() != null) {
                searchStaffUserResponseValue.setTaskSupervisor(caseWorkerProfile.getTaskSupervisor());
            }
            if (caseWorkerProfile.getCaseAllocator() != null) {
                searchStaffUserResponseValue.setCaseAllocator(caseWorkerProfile.getCaseAllocator());
            }
            if (caseWorkerProfile.getSuspended() != null) {
                searchStaffUserResponseValue.setSuspended(caseWorkerProfile.getSuspended());
            }
            if (caseWorkerProfile.getUserAdmin() != null) {
                searchStaffUserResponseValue.setStaffAdmin(caseWorkerProfile.getUserAdmin());
            }
            searchStaffUserResponse.add(
                    searchStaffUserResponseValue
            );
        });

        return searchStaffUserResponse;
    }

    private List<Role> mapRolesToDto(List<CaseWorkerRole> caseWorkerRoles) {
        List<Role> rolesDto = new ArrayList<>();
        for (CaseWorkerRole caseWorkerRole : caseWorkerRoles) {
            var roleDto = Role.builder()
                .roleId(caseWorkerRole.getRoleId().toString())
                .roleName(caseWorkerRole.getRoleType().getDescription())
                .isPrimary(caseWorkerRole.getPrimaryFlag())
                .build();

            rolesDto.add(roleDto);
        }
        return rolesDto;
    }

    private List<Location> mapBaseLocationsToDto(List<CaseWorkerLocation> caseWorkerLocations) {
        List<Location> locations = new ArrayList<>();
        for (CaseWorkerLocation caseWorkerLocation : caseWorkerLocations) {
            var location = Location.builder()
                .baseLocationId(caseWorkerLocation.getLocationId())
                .locationName(caseWorkerLocation.getLocation())
                .isPrimary(caseWorkerLocation.getPrimaryFlag())
                .build();

            locations.add(location);
        }
        return locations;
    }

    private List<ServiceResponse> mapServicesToDto(List<CaseWorkerWorkArea> caseWorkerWorkAreas) {
        List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (CaseWorkerWorkArea caseWorkerWorkArea : caseWorkerWorkAreas) {
            var service = ServiceResponse.builder()
                .service(caseWorkerWorkArea.getAreaOfWork())
                .serviceCode(caseWorkerWorkArea.getServiceCode())
                .build();

            serviceResponses.add(service);
        }
        return serviceResponses;
    }

    private List<SkillResponse> mapSkillsToDto(List<CaseWorkerSkill> caseWorkerSkills) {

        List<SkillResponse> skills = new ArrayList<>();
        caseWorkerSkills.forEach(caseWorkerSkill -> {

            for (Skill skill : caseWorkerSkill.getSkills()) {
                var skillResponse = SkillResponse.builder()
                        .skillId(skill.getSkillId())
                        .description(skill.getDescription())
                        .build();

                skills.add(skillResponse);
            }
        });



        return skills;
    }


    @Override
    public StaffWorkerSkillResponse getServiceSkills() {
        List<Skill> skills = null;
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        try {
            List<SkillDTO> skillData = new ArrayList<>();
            skills = skillRepository.findAll();
            Optional<List<Skill>> skillsOptional = Optional.ofNullable(skills);
            if (skillsOptional.isPresent()) {
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

    /**
     * To convert skills data to ServiceSkills.
     * @param skillData List of skills
     * @return List of ServiceSkill
     */
    public List<ServiceSkill> mapSkillToServicesSkill(List<SkillDTO> skillData) {

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
                (key, value) -> {
                    ServiceSkill serviceSkill = new ServiceSkill();
                    serviceSkill.setId(key);
                    serviceSkill.setSkills(value);
                    serviceSkills.add(serviceSkill);
                }
        );
        return serviceSkills;

    }

    private List<SkillDTO> mergeSkillsWithDuplicateServiceIds(List<SkillDTO> existingResults,
                                                              List<SkillDTO> newResults) {
        List<SkillDTO> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }


}
