package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerStaticValueRepositoryAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffProfileCreateUpdateUtil {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;

    @Autowired
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;



    // get the userTypeId by description.
    public Long getUserTypeIdByDesc(String userTypeReq) {
        Optional<Long> userTypeId = caseWorkerStaticValueRepositoryAccessor
                .getUserTypes()
                .stream().filter(userType ->
                        userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
                .map(UserType::getUserTypeId).findFirst();
        return userTypeId.orElse(0L);
    }

    public List<CaseWorkerLocation> mapStaffLocationRequest(String idamId,
                                                                 StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        staffProfileRequest.getBaseLocations().forEach(location -> {

            CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(idamId,
                    location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
            cwLocations.add(caseWorkerLocation);
        });
        return cwLocations;
    }

    public List<CaseWorkerRole> mapStaffRoleRequestMapping(String idamId,
                                                                StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        staffProfileRequest.getRoles().forEach(role -> caseWorkerStaticValueRepositoryAccessor
                .getRoleTypes()
                .stream().filter(roleType ->
                        role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .forEach(roleType -> {
                    CaseWorkerRole workerRole = new CaseWorkerRole(idamId, roleType.getRoleId(), role.isPrimaryFlag());
                    caseWorkerRoles.add(workerRole);
                }));
        return caseWorkerRoles;
    }

    public List<CaseWorkerWorkArea> mapStaffAreaOfWork(StaffProfileCreationRequest profileRequest,
                                                       String idamId) {
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        profileRequest.getServices().forEach(caseWorkerWorkAreaRequest -> {
            CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea(idamId,
                    caseWorkerWorkAreaRequest.getService(), caseWorkerWorkAreaRequest.getServiceCode());
            caseWorkerWorkAreas.add(caseWorkerWorkArea);
        });
        return caseWorkerWorkAreas;
    }

    public List<CaseWorkerSkill> mapStaffSkillRequestMapping(String idamId,
                                                                  StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerSkill> caseWorkerSkills = new ArrayList<>();
        staffProfileRequest.getSkills().forEach(skill -> caseWorkerStaticValueRepositoryAccessor
                .getSkills()
                .stream().filter(skills ->
                        skill.getDescription().equalsIgnoreCase(skills.getDescription().trim()))
                .forEach(skills -> {
                    CaseWorkerSkill workerSkill = new CaseWorkerSkill(idamId, skills.getSkillId());
                    caseWorkerSkills.add(workerSkill);
                }));
        return caseWorkerSkills;

    }

    // get the roles that needs to send to idam based on the roleType in the request.
    public Set<String> getUserRolesByRoleId(StaffProfileCreationRequest staffProfileRequest) {

        // get Roles Types
        List<RoleType> roleTypeList = new ArrayList<>();
        staffProfileRequest.getRoles().forEach(role -> roleTypeList.addAll(
                caseWorkerStaticValueRepositoryAccessor
                        .getRoleTypes()
                        .stream()
                        .filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                        .toList())
        );

        // get work area codes
        List<String> serviceCodes = staffProfileRequest.getServices()
                .stream()
                .map(CaseWorkerServicesRequest::getServiceCode)
                .toList();


        // get all assoc records matching role id and service code, finally return idam roles associated
        Set<String> matchedRoles = roleAssocRepository.findByRoleTypeInAndServiceCodeIn(roleTypeList, serviceCodes)
                .stream()
                .map(CaseWorkerIdamRoleAssociation::getIdamRole)
                .collect(Collectors.toSet());
        log.info("{}:: roles matched from assoc :: {}", loggingComponentName, matchedRoles);
        return matchedRoles;
    }

}
