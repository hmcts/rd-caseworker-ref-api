package uk.gov.hmcts.reform.cwrdapi.service.impl;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class StaffRefDataServiceTest {

    @Mock
    private UserTypeRepository userTypeRepository;
    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;
    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepository;
    @Mock
    CaseWorkerLocationRepository caseWorkerLocationRepository;
    @Mock
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;
    @Mock
    CaseWorkerRoleRepository caseWorkerRoleRepository;
    @Mock
    CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;
    @Mock
    private UserProfileFeignClient userProfileFeignClient;
    @Mock
    private TopicPublisher topicPublisher;
    @Mock
    CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;
    @Mock
    IValidationService validationServiceFacade;
    @Mock
    SkillRepository skillRepository;
    @Mock
    IJsrValidatorInitializer validateStaffProfile;
    @Mock
    private StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;
    @Mock
    StaffAuditRepository staffAuditRepository;

    private StaffProfileCreationRequest staffProfileCreationRequest;
    private StaffProfileCreationResponse staffProfileCreationRespone = new StaffProfileCreationResponse();
    private RoleType roleType;
    private UserType userType;
    private Skill skill;
    private CaseWorkerProfile caseWorkerProfile;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        StaffProfileRoleRequest caseWorkerRoleRequest =
                new StaffProfileRoleRequest(1,"testRole1", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId("1L")
                .description("training")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .roles(singletonList(caseWorkerRoleRequest))
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(caseWorkerRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();

        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");

        roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("testUser1");

        skill = new Skill();
        skill.setSkillId(1L);
        skill.setDescription("training");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_All() {
        when(userTypeRepository.findAll())
                .thenReturn(prepareUserTypeResponse());
        var userTypes = staffRefDataServiceImpl
                .fetchUserTypes();
        verifyAllUserTypes(userTypes);
        //added to verify conetent
        assertTrue(verifyAllUserTypesContent(userTypes, prepareUserTypeResponse()));
        //added to verify counts
        assertEquals(4, userTypes.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_one() {
        when(userTypeRepository.findAll()).thenReturn(prepareUserTypeResponse());
        var staffRefDataUserTypesResponses = (List<UserType>) staffRefDataServiceImpl
                .fetchUserTypes();
        assertFalse(verifyCurrentUserTypes(staffRefDataUserTypesResponses.get(0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_NoDataFound() {
        var userTypes = prepareUserTypeResponse();
        userTypes.clear();
        when(userTypeRepository.findAll()).thenReturn(userTypes);
        var staffRefDataUserTypesResponses = (List<UserType>) staffRefDataServiceImpl
                .fetchUserTypes();
        assertTrue(staffRefDataUserTypesResponses.isEmpty());
    }

    private List<UserType> prepareUserTypeResponse() {
        var userTypeArrayList = new ArrayList<UserType>();
        userTypeArrayList.add(new UserType(1L, "User Type 1"));
        userTypeArrayList.add(new UserType(2L, "User Type 2"));
        userTypeArrayList.add(new UserType(3L, "User Type 3"));
        userTypeArrayList.add(new UserType(4L, "User Type 4"));

        return userTypeArrayList;
    }

    private void verifyAllUserTypes(List<UserType> userTypes) {
        boolean isInvalidResponse = userTypes
                .stream()
                .anyMatch(userType -> verifyCurrentUserTypes(userType));
        assertFalse(isInvalidResponse);
    }

    private boolean verifyCurrentUserTypes(UserType userType) {
        return userType.getUserTypeId() == null || userType.getDescription() == null;
    }

    private boolean verifyAllUserTypesContent(List<UserType> userTypes, List<UserType> prepareUserTypeResponse) {
        for (int i = 0; i < prepareUserTypeResponse.size(); i++) {
            UserType staffRefDataUserType = prepareUserTypeResponse.get(i);
            Optional<UserType> userType = userTypes.stream().filter(e ->
                    e.getUserTypeId().equals(staffRefDataUserType.getUserTypeId())
                            && e.getDescription().equals(staffRefDataUserType.getDescription())).findAny();
            if (!userType.isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Test
    void test_saveStaffProfileValidationAudit() {

        validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                caseWorkerProfile.getCaseWorkerId(),staffProfileCreationRequest);
        verify(staffAuditRepository, times(0)).save(any());
    }


    @Test
    void test_saveStaffProfileAlreadyPresent() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_createUserProfileRequest() {
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequest_StaffAdmin() {
        staffProfileCreationRequest.setStaffAdmin(true);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequestEmptyRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(EMPTY_SET);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }



    @Test
    void test_createUserProfileRequestNullRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(null);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    void test_publishCaseWorkerDataToTopic() {
        ReflectionTestUtils.setField(staffRefDataServiceImpl, "caseWorkerDataPerMessage", 1);
        staffProfileCreationRespone.setCaseWorkerId("1");
        staffRefDataServiceImpl.publishStaffProfileToTopic(staffProfileCreationRespone);
        verify(topicPublisher, times(1)).sendMessage(any());
    }
}