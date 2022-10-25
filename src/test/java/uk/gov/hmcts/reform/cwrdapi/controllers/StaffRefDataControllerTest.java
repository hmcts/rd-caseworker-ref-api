package uk.gov.hmcts.reform.cwrdapi.controllers;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.EmptyRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.validateSearchUserProfileResponse;

@ExtendWith(MockitoExtension.class)
class StaffRefDataControllerTest {


    @Mock
    StaffRefDataService staffRefDataService;



    SearchStaffUserResponse searchResponse;

    StaffRefDataUserTypesResponse srResponse;
    ResponseEntity<Object> responseEntity;


    ResponseEntity<List<SearchStaffUserResponse>> advancedSearchResponse;


    SearchRequest searchReq;

    @InjectMocks
    private StaffRefDataController staffRefDataController;
    @Mock
    StaffRefDataService staffProfileService;
    List<UserType> userTypes = null;
    StaffProfileCreationRequest request;
    StaffProfileCreationResponse response;

    @BeforeEach
    void setUp() {

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
                .build();
        request = StaffProfileCreationRequest.staffProfileCreationRequest().build();

        response = StaffProfileCreationResponse.builder()
                .caseWorkerId(UUID.randomUUID().toString())
                .build();

        responseEntity = new ResponseEntity<>(srResponse, null, HttpStatus.OK);

        userTypes = new ArrayList<>();
        userTypes.add(new UserType(1L, "Test"));
        userTypes.add(new UserType(2L, "Test 2"));

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
                .build();

        List<ServiceResponse> services = new ArrayList<>();
        List<Role> roles = new ArrayList<>();
        List<Location> baseLocations = new ArrayList<>();
        List<SkillResponse> skills = new ArrayList<>();



        searchResponse = SearchStaffUserResponse.builder()
                .firstName("firstName")
                .lastName("lastName")
                .emailId("emailId")
                .services(services)
                .region("region")
                .regionId(123)
                .roles(roles)
                .taskSupervisor(true)
                .caseAllocator(true)
                .suspended(false)
                .staffAdmin(true)
                .baseLocations(baseLocations)
                .skills(skills).build();



        searchReq = SearchRequest.builder()
                .role("case allocator")
                .jobTitle("8436")
                .location("8338")
                .serviceCode("BFA1")
                .skill("1")
                .userType("1")
                .build();


        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_return_service_skills_with_status_code_200() {
        StaffWorkerSkillResponse staffWorkerSkillResponse =
                new StaffWorkerSkillResponse();
        when(staffRefDataService.getServiceSkills()).thenReturn(staffWorkerSkillResponse);
        ResponseEntity<StaffWorkerSkillResponse> responseEntity =
                staffRefDataController.retrieveAllServiceSkills();

        assertNotNull(responseEntity);

        assertThat(staffWorkerSkillResponse.getServiceSkills()).isNull();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


        verify(staffRefDataService, times(1))
                .getServiceSkills();
    }

    @Test
    void should_return_200_when_no_skills_found() {
        StaffWorkerSkillResponse staffWorkerSkillResponse =
                new StaffWorkerSkillResponse();
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        when(staffRefDataService.getServiceSkills()).thenReturn(staffWorkerSkillResponse);
        ResponseEntity<StaffWorkerSkillResponse> responseEntity =
                staffRefDataController.retrieveAllServiceSkills();

        assertNotNull(responseEntity);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0,responseEntity.getBody().getServiceSkills().size());
    }





    @Test
    void shouldFetchUserTypes() {
        responseEntity = ResponseEntity.status(200).body(null);
        when(staffRefDataService.fetchUserTypes())
                .thenReturn(userTypes);

        ResponseEntity<?> actual = staffRefDataController.fetchUserTypes();
        final StaffRefDataUserTypesResponse actualResponse = (StaffRefDataUserTypesResponse) actual.getBody();
        assertNotNull(actual);
        verify(staffRefDataService, times(1))
                .fetchUserTypes();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
        assertEquals((userTypes.size()), actualResponse.getUserTypes().size());
        List<StaffRefDataUserType> actualResultUserType = new ArrayList<>(actualResponse.getUserTypes());
        //assert all attributes lists
        assertTrue(verifyAllUserTypes(actualResultUserType, userTypes));
    }

    @Test
    void shouldFetchEmptyUserTypes() {
        responseEntity = ResponseEntity.status(200).body(null);
        userTypes.clear();
        when(staffRefDataService.fetchUserTypes())
                .thenReturn(userTypes);

        ResponseEntity<?> actual = staffRefDataController.fetchUserTypes();
        final StaffRefDataUserTypesResponse actualResponse = (StaffRefDataUserTypesResponse) actual.getBody();
        assertNotNull(actual);
        verify(staffRefDataService, times(1))
                .fetchUserTypes();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
        assertEquals(0, (actualResponse.getUserTypes().size()));
        Assertions.assertArrayEquals(actualResponse.getUserTypes().toArray(), userTypes.toArray());
    }


    @ParameterizedTest
    @ValueSource(strings = {"BFA1,BFA2","BFA1"})
    void should_return_staffProfiles_search_with_service_code_status_code_200(String serviceId) {

        List<ServiceResponse> services = List.of(ServiceResponse.builder().serviceCode("BFA1").service("avd").build());
        searchResponse.setServices(services);

        searchReq = SearchRequest.builder()
                .serviceCode(serviceId)
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"s*_"})
    void should_return_staffProfiles_search_with_invalid_service_code_400(String serviceId) {
        searchReq = SearchRequest.builder()
                .serviceCode(serviceId)
                .build();
        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Invalid Service ids: " + serviceId, ex.getMessage());
    }

    @Test
    void should_return_staffProfiles_search_sortingOrder_status_code_200() {
        List<ServiceResponse> services = List.of(ServiceResponse.builder().serviceCode("BFA1").service("avd").build());
        searchResponse.setServices(services);

        SearchStaffUserResponse searchResponse2 = SearchStaffUserResponse.builder()
                .firstName("firstName")
                .lastName("mastName")
                .emailId("emailId")
                .region("region")
                .regionId(123)
                .build();
        searchResponse2.setServices(services);

        searchReq = SearchRequest.builder()
                .serviceCode("BFA1")
                .build();

        advancedSearchResponse = new ResponseEntity<>(Arrays.asList(searchResponse,searchResponse2), null,
                HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));

    }



    @ParameterizedTest
    @ValueSource(strings = {"101633,231596","231596"})
    void should_return_staffProfiles_search_with_location_status_code_200(String location) {
        List<Location> baseLocations = List.of(Location.builder().baseLocationId(231596).locationName("avd").build());
        searchResponse.setBaseLocations(baseLocations);

        searchReq = SearchRequest.builder()
                .location(location)
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcde,fjkui","*__"})
    void should_return_staffProfiles_search_with_invalid_input_location_status_code_400(String location) {
        searchReq = SearchRequest.builder()
                .location(location)
                .build();

        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Invalid location type ids: " + location, ex.getMessage());

    }


    @ParameterizedTest
    @ValueSource(strings = {"1"})
    void should_return_staffProfiles_search_with_usertype_status_code_200(String userType) {
        searchResponse.setUserType("1");
        searchReq = SearchRequest.builder()
                .userType(userType)
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response = staffRefDataController
                .searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab..c","*__","sdf sdf"})
    void should_return_staffProfiles_search_with_invalid_input_usertype_status_code_400(String userType) {

        searchReq = SearchRequest.builder()
                .userType(userType)
                .build();

        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Param contains special characters. ", ex.getMessage());
    }



    @ParameterizedTest
    @ValueSource(strings = {"1"})
    void should_return_staffProfiles_search_with_jobTitle_status_code_200(String jobTitileId) {
        List<Role> roles = List.of(Role.builder().roleId("1").roleName("avd").build());
        searchResponse.setRoles(roles);
        searchReq = SearchRequest.builder()
                .jobTitle(jobTitileId)
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"avd...x","_*kd"})
    void should_return_staffProfiles_search_with_invalid_input_jobTitle_status_code_400(String jobTitileId) {
        searchReq = SearchRequest.builder()
                .jobTitle(jobTitileId)
                .build();

        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Param contains special characters. ", ex.getMessage());
    }




    @Test
    void should_return_staffProfiles_search_with_skillId_status_code_200() {
        List<SkillResponse> skills = List.of(SkillResponse.builder().skillId(1L).description("avd").build());
        searchResponse.setSkills(skills);
        searchReq = SearchRequest.builder()
                .skill("1")
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"av..dx","_*kd"})
    void should_return_staffProfiles_search_with_invalid_skillId_status_code_400(String skillId) {
        searchReq = SearchRequest.builder()
                .skill(skillId)
                .build();

        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Param contains special characters. ", ex.getMessage());

    }



    @ParameterizedTest
    @ValueSource(strings = {"task supervisor,case allocator,staff administrator"})
    void should_return_staffProfiles_search_with_role_status_code_200(String roleId) {
        searchResponse.setTaskSupervisor(true);
        searchResponse.setCaseAllocator(true);
        searchResponse.setStaffAdmin(true);

        searchReq = SearchRequest.builder()
                .role(roleId)
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response = staffRefDataController
                .searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tasksupervisor,case_allocator,abcd"})
    void should_return_staffProfiles_search_with_invalid_role_status_code_400(String roleId) {

        searchReq = SearchRequest.builder()
                .role(roleId)
                .build();

        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Invalid search string. Please input a valid string.", ex.getMessage());

    }

    @Test
    void should_return_staffProfiles_search_with_all_params_status_code_200() {
        searchResponse.setUserType("1");

        List<Role> roles = List.of(Role.builder().roleId("1").roleName("avd").build());
        searchResponse.setRoles(roles);

        List<Location> baseLocations = List.of(Location.builder().baseLocationId(231596).locationName("avd").build());
        searchResponse.setBaseLocations(baseLocations);

        List<ServiceResponse> services = List.of(ServiceResponse.builder().serviceCode("BFA1").service("avd").build());
        searchResponse.setServices(services);

        List<SkillResponse> skills = List.of(SkillResponse.builder().skillId(1L).description("avd").build());
        searchResponse.setSkills(skills);

        searchResponse.setTaskSupervisor(true);
        searchResponse.setCaseAllocator(true);
        searchResponse.setStaffAdmin(true);

        searchReq = SearchRequest.builder()
                .serviceCode("BFA1")
                .location("231596")
                .userType("1")
                .jobTitle("1")
                .skill("1")
                .role("task supervisor,case allocator,staff administrator")
                .build();

        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response =
                staffRefDataController.searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertTrue(validateSearchUserProfileResponse(response,searchReq));
    }

    @Test
    void should_return_staffProfiles_with_no_data_found_status_code_200() {
        advancedSearchResponse = new ResponseEntity<>(new ArrayList<>(), null, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response = staffRefDataController
                .searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
    }

    @Test
    void should_return_staffProfiles_validateTotalRecords_status_code_200() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("total-records","2");
        advancedSearchResponse = new ResponseEntity<>(List.of(searchResponse,searchResponse), headers, HttpStatus.OK);
        when(staffRefDataService.retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class)))
                .thenReturn(advancedSearchResponse);
        ResponseEntity<List<SearchStaffUserResponse>> response = staffRefDataController
                .searchStaffProfile(1,2, searchReq);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        int totalRecords = 0;
        if (response.getHeaders().get("total-records") != null) {
            totalRecords = Integer.parseInt(response.getHeaders().get("total-records").get(0));
        }
        assertThat(totalRecords).isEqualTo(2);
        verify(staffRefDataService, times(1))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
    }


    @Test
    void should_return_staffProfiles_with_status_code_400_page_number_0() {
        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(0,2, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("The field Page Number is invalid. Please provide a valid value.", ex.getMessage());

    }

    @Test
    void should_return_staffProfiles_with_status_code_400_page_size_lessThan1() {
        Exception ex = assertThrows(InvalidRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,0, searchReq));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("The field Page Size is invalid. Please provide a valid value.", ex.getMessage());

    }


    @Test
    void should_return_staffProfiles_with_status_code_400_at_least_one_param_required() {
        SearchRequest req = SearchRequest.builder().build();
        Exception ex = assertThrows(EmptyRequestException.class, () -> staffRefDataController
                .searchStaffProfile(2,1, req));
        verify(staffRefDataService, times(0))
                .retrieveStaffProfile(eq(searchReq), Mockito.any(PageRequest.class));
        assertNotNull(ex);
        assertEquals("Unexpected character", ex.getMessage());

    }









    private boolean verifyAllUserTypes(List<StaffRefDataUserType> actualResultUserType, List<UserType> userTypes) {
        for (int i = 0; i < actualResultUserType.size(); i++) {
            StaffRefDataUserType staffRefDataUserType = actualResultUserType.get(i);
            Optional<UserType> userType = userTypes.stream().filter(e ->
                    e.getUserTypeId().equals(staffRefDataUserType.getId())
                            && e.getDescription().equals(staffRefDataUserType.getCode())).findAny();
            if (!userType.isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Test
    void should_return_staffCreateResponse_with_status_code_200() {

        ResponseEntity<StaffProfileCreationResponse> actual = staffRefDataController
                .createStaffUserProfile(request);
        assertThat(actual.getStatusCodeValue()).isEqualTo(201);
    }
}
