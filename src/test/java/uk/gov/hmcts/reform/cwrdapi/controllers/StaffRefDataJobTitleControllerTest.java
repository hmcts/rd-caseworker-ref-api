package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class StaffRefDataJobTitleControllerTest {

    StaffRefDataService staffRefDataServiceMock;
    StaffRefJobTitleResponse srJobTitleResponse;
    ResponseEntity<Object> responseEntity;
    @InjectMocks
    private StaffRefDataController staffRefDataController;

    List<RoleType> roleType = null;

    @BeforeEach
    void setUp() {
        staffRefDataServiceMock = mock(StaffRefDataService.class);

        srJobTitleResponse = StaffRefJobTitleResponse
                .builder()
                .jobTitles(Collections.emptyList())
                .build();

        responseEntity = new ResponseEntity<>(srJobTitleResponse, null, HttpStatus.OK);

        roleType = new ArrayList<>();
        roleType.add(new RoleType(1L,"Test"));
        roleType.add(new RoleType(2L,"Test 2"));

        srJobTitleResponse = StaffRefJobTitleResponse
                .builder()
                .jobTitles(Collections.emptyList())
                .build();
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void shouldFetchJobTitles() {
        responseEntity = ResponseEntity.status(200).body(null);
        when(staffRefDataServiceMock.getJobTitles())
                .thenReturn(roleType);

        ResponseEntity<?> actual = staffRefDataController.retrieveJobTitles();

        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .getJobTitles();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }


    @Test
    void shouldFetchNoJobTitles() {
        responseEntity = ResponseEntity.status(404).body(null);
        roleType.clear();
        when(staffRefDataServiceMock.getJobTitles())
                .thenReturn(roleType);

        ResponseEntity<?> actual = staffRefDataController.retrieveJobTitles();

        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .getJobTitles();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
    }
}
