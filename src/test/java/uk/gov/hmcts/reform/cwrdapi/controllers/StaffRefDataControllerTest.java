package uk.gov.hmcts.reform.cwrdapi.controllers;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffRefDataControllerTest {


    @Mock
    StaffRefDataService staffRefDataService;



    StaffRefDataService staffRefDataServiceMock;
    StaffRefDataUserTypesResponse srResponse;
    ResponseEntity<Object> responseEntity;
    @InjectMocks
    private StaffRefDataController staffRefDataController;

    List<UserType> userTypes = null;

    @BeforeEach
    void setUp() {
        staffRefDataServiceMock = mock(StaffRefDataService.class);

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
                .build();

        responseEntity = new ResponseEntity<>(srResponse, null, HttpStatus.OK);

        userTypes = new ArrayList<>();
        userTypes.add(new UserType(1L, "Test"));
        userTypes.add(new UserType(2L, "Test 2"));

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
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


}
