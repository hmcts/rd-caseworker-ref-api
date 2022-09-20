package uk.gov.hmcts.reform.cwrdapi.service.impl;

import java.util.ArrayList;
import java.util.List;

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
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

@Service
@Slf4j
public class StaffRefDataServiceImpl implements StaffRefDataService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;
    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Override
    public ResponseEntity<Object> retrieveStaffUserByName(String searchString, PageRequest pageRequest) {

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
        caseWorkerProfiles.forEach(caseWorkerProfile -> searchStaffUserResponse.add(
            SearchStaffUserResponse.builder()
                .firstName(caseWorkerProfile.getFirstName())
                .lastName(caseWorkerProfile.getLastName())
                .emailId(caseWorkerProfile.getEmailId())
                .services(mapServicesToDto(caseWorkerProfile.getCaseWorkerWorkAreas()))
                .region(caseWorkerProfile.getRegion())
                .regionId(caseWorkerProfile.getRegionId())
                .roles(mapRolesToDto(caseWorkerProfile.getCaseWorkerRoles()))
                .taskSupervisor(caseWorkerProfile.getTaskSupervisor())
                .caseAllocator(caseWorkerProfile.getCaseAllocator())
                .suspended(caseWorkerProfile.getSuspended())
                .staffAdmin(caseWorkerProfile.getUserAdmin())
                .baseLocations(mapBaseLocationsToDto(caseWorkerProfile.getCaseWorkerLocations()))
                .userType(caseWorkerProfile.getUserType().getDescription())
                .skills(mapSkillsToDto(caseWorkerProfile.getCaseWorkerSkills()))
                .build()
        ));

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

    private List<SkillResponse> mapSkillsToDto(List<Skill> caseWorkerSkills) {
        List<SkillResponse> skills = new ArrayList<>();
        for (Skill caseWorkerSkill : caseWorkerSkills) {
            var skill = SkillResponse.builder()
                .skillId(caseWorkerSkill.getSkillId())
                .description(caseWorkerSkill.getDescription())
                .build();

            skills.add(skill);
        }
        return skills;
    }
}
