package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.transaction.Transactional;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CASE_ALLOCATOR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_FIRST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CW_LAST_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TASK_SUPERVISOR;

@Transactional
public class CaseWorkerProfileRepositoryIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;


    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    SearchRequest searchReq;

    @BeforeEach
    public void setUpClient() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        super.setUpClient();
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
        mockJwtToken(ROLE_STAFF_ADMIN);
    }

    @AfterEach
    public void cleanUpEach() {
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }



    @Test
    void success_return_staff_users_from_database() {

        List<String> serviceCodes = ImmutableList.of("ABA1");
        List<Integer> locationId = ImmutableList.of(12345);
        List<String> roles = ImmutableList.of("task supervisor,case allocator");

        PageRequest pageRequest = PageRequest.of(1, 1,
                Sort.by(Sort.DEFAULT_DIRECTION, CW_LAST_NAME, CW_FIRST_NAME));


        searchReq = SearchRequest.builder()
                .serviceCode("ABA1")
                .location("12345")
                .userType("1")
                .jobTitle("2")
                .role("task supervisor,case allocator,staff administrator")
                .skill("9")
                .build();
        createCaseWorkerProfiles();

        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepository.findByCaseWorkerProfiles(searchReq, serviceCodes, locationId,
                        roles.contains(TASK_SUPERVISOR), roles.contains(CASE_ALLOCATOR), roles.contains(STAFF_ADMIN),
                        pageRequest);
        assertThat(pageable).isNotNull();
        assertThat(pageable.getTotalElements()).isEqualTo(5);
        assertThat(pageable.getContent()).hasSize(1);
        assertTrue(validateSearchUserProfileResponse(pageable,searchReq));

    }

    @Test
    void no_data_found_in_database() {

        List<String> serviceCodes = ImmutableList.of("ABA1");
        List<Integer> locationId = ImmutableList.of(12345);
        List<String> roles = ImmutableList.of("task supervisor,case allocator");

        PageRequest pageRequest = PageRequest.of(1, 1,
                Sort.by(Sort.DEFAULT_DIRECTION, CW_LAST_NAME, CW_FIRST_NAME));


        searchReq = SearchRequest.builder()
                .serviceCode("asdf")
                .location("115")
                .userType("2")
                .jobTitle("2")
                .role("task supervisor,case allocator,staff administrator")
                .skill("1")
                .build();
        createCaseWorkerProfiles();

        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepository.findByCaseWorkerProfiles(searchReq, serviceCodes, locationId,
                        roles.contains(TASK_SUPERVISOR), roles.contains(CASE_ALLOCATOR), roles.contains(STAFF_ADMIN),
                        pageRequest);
        assertThat(pageable).isNotNull();
        assertThat(pageable.getTotalElements()).isZero();

    }


    @Test
    void should_return_staff_user_with_status_code_200_when_skill_are_empty() {
        mockJwtToken(ROLE_STAFF_ADMIN);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest staffProfileCreationRequest = caseworkerReferenceDataClient
                .createStaffProfileCreationRequest();
        staffProfileCreationRequest.setSkills(null);
        Map<String, Object> staffProfileResponse = caseworkerReferenceDataClient
                .createStaffProfile(staffProfileCreationRequest,ROLE_STAFF_ADMIN);
        assertThat(staffProfileResponse).containsEntry("http_status", "201 CREATED");

        List<String> serviceCodes = ImmutableList.of("ABA1");
        List<Integer> locationId = ImmutableList.of(12345);
        List<String> roles = ImmutableList.of("task supervisor,case allocator");

        PageRequest pageRequest = PageRequest.of(0, 1,
                Sort.by(Sort.DEFAULT_DIRECTION, CW_LAST_NAME, CW_FIRST_NAME));

        searchReq = SearchRequest.builder()
                .serviceCode("ABA1")
                .location("12345")
                .userType("1")
                .jobTitle("2")
                .role("task supervisor,case allocator,staff administrator")
                .build();

        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepository.findByCaseWorkerProfiles(searchReq, serviceCodes, locationId,
                        roles.contains(TASK_SUPERVISOR), roles.contains(CASE_ALLOCATOR), roles.contains(STAFF_ADMIN),
                        pageRequest);
        assertThat(pageable).isNotNull();
        assertThat(pageable.getTotalElements()).isEqualTo(1);
        assertThat(pageable.getContent()).hasSize(1);
        assertThat(pageable.getContent().get(0).getCaseWorkerSkills()).isEmpty();
        assertTrue(validateSearchUserProfileResponse(pageable,searchReq));

    }



    public void createCaseWorkerTestData() {
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest staffProfileCreationRequest = caseworkerReferenceDataClient
                .createStaffProfileCreationRequest();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(staffProfileCreationRequest,ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "201 CREATED");

    }


    private void createCaseWorkerProfiles() {
        IntStream.range(0,5).forEach(i -> createCaseWorkerTestData());
    }

    public static boolean validateSearchUserProfileResponse(Page<CaseWorkerProfile> pageable,
                                                            SearchRequest searchReq) {
        List<CaseWorkerProfile> caseWorkerProfiles = pageable.getContent();
        List<CaseWorkerProfile> validResponse;
        validResponse = caseWorkerProfiles.stream()
                .filter(Objects::nonNull)
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getServiceCode()).isEmpty()) {
                        return true;
                    }
                    if (Optional.ofNullable(caseWorkerProfile.getCaseWorkerWorkAreas()).isEmpty()) {
                        return false;
                    }

                    return caseWorkerProfile.getCaseWorkerWorkAreas().stream()
                            .anyMatch(service -> Optional.ofNullable(service).isPresent()
                                    && searchReq.getServiceCode().toLowerCase().contains(service.getServiceCode()
                                    .toLowerCase())
                            );
                })
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getLocation()).isEmpty()) {
                        return true;
                    }
                    if (Optional.ofNullable(caseWorkerProfile.getCaseWorkerLocations()).isEmpty()) {
                        return false;
                    }

                    return caseWorkerProfile.getCaseWorkerLocations().stream()

                            .anyMatch(location -> Optional.ofNullable(location).isPresent()
                                    && searchReq.getLocation().toLowerCase().contains(String.valueOf(location
                                    .getLocationId()))
                            );
                })
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getUserType()).isEmpty()) {
                        return true;
                    }
                    return Optional.ofNullable(caseWorkerProfile.getUserType()).isPresent();
                })
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getJobTitle()).isEmpty()) {
                        return true;
                    }
                    if (Optional.ofNullable(caseWorkerProfile.getCaseWorkerRoles()).isEmpty()) {
                        return false;
                    }
                    return caseWorkerProfile.getCaseWorkerRoles().stream()

                            .anyMatch(roles -> Optional.ofNullable(roles).isPresent()
                                    && roles.getRoleId().toString().toLowerCase().contains(searchReq.getJobTitle()
                                    .toLowerCase())
                            );
                })
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getSkill()).isEmpty()) {
                        return true;
                    }
                    if (Optional.ofNullable(caseWorkerProfile.getCaseWorkerSkills()).isEmpty()) {
                        return false;
                    }
                    return caseWorkerProfile.getCaseWorkerSkills().stream()

                            .anyMatch(skill -> Optional.ofNullable(skill).isPresent()
                                    && skill.getSkillId() == Integer.parseInt(searchReq.getSkill())
                            );
                })
                .filter(caseWorkerProfile -> {
                    if (Optional.ofNullable(searchReq.getRole()).isEmpty()) {
                        return true;
                    }
                    boolean valid = searchReq.getRole().contains("task supervisor") && caseWorkerProfile
                            .getTaskSupervisor();

                    if (searchReq.getRole().contains("case allocator") && caseWorkerProfile
                            .getCaseAllocator()) {
                        valid = true;
                    }
                    if (searchReq.getRole().contains("Staff Administrator") && caseWorkerProfile
                            .getUserAdmin()) {
                        valid = true;
                    }


                    return valid;
                })


                .toList();
        Comparator<CaseWorkerProfile> comparator
                = Comparator.comparing(CaseWorkerProfile::getLastName);

        List<CaseWorkerProfile> sorted = new ArrayList<>(validResponse);
        sorted.sort(comparator);

        if (!validResponse.equals(sorted)) {
            return false;
        }

        return validResponse.size() == caseWorkerProfiles.size();

    }

}
