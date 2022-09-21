package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffRefDataControllerTest {

    StaffRefDataService staffRefDataServiceMock;
    StaffRefDataUserTypesResponse srResponse;
    ResponseEntity<Object> responseEntity;
    @InjectMocks
    private StaffRefDataController staffRefDataController;

    List<UserType> userTypes=null;

    @BeforeEach
    void setUp() {
        staffRefDataServiceMock = mock(StaffRefDataService.class);

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
                .build();

        responseEntity = new ResponseEntity<>(srResponse, null, HttpStatus.OK);

        userTypes = new ArrayList<>();
        userTypes.add(new UserType(1L,"Test"));
        userTypes.add(new UserType(2L,"Test 2"));

        srResponse = StaffRefDataUserTypesResponse
                .builder()
                .userTypes(Collections.emptyList())
                .build();
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void shouldFetchUserTypes() {
        responseEntity = ResponseEntity.status(200).body(null);
        when(staffRefDataServiceMock.fetchUserTypes())
                .thenReturn(userTypes);

        ResponseEntity<?> actual = staffRefDataController.fetchUserTypes();

        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .fetchUserTypes();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }


    @Test
    void shouldFetchNoUserTypes() {
        responseEntity = ResponseEntity.status(404).body(null);
        userTypes.clear();
        when(staffRefDataServiceMock.fetchUserTypes())
                .thenReturn(userTypes);

        ResponseEntity<?> actual = staffRefDataController.fetchUserTypes();

        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .fetchUserTypes();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }

}
