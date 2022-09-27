package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
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
class StaffRefDataJobTitleControllerTest {

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
        roleType.add(new RoleType(1L, "Test"));
        roleType.add(new RoleType(2L, "Test 2"));

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
        final StaffRefJobTitleResponse actualResponse = (StaffRefJobTitleResponse) actual.getBody();
        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .getJobTitles();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
        assertEquals((roleType.size()), actualResponse.getJobTitles().size());
        List<StaffRefDataJobTitle> actualResultUserType = new ArrayList<>(actualResponse.getJobTitles());
        //assert all attributes lists
        assertTrue(verifyAllRoleTypes(actualResultUserType, roleType));
    }


    @Test
    void shouldFetchEmptyJobTitles() {
        responseEntity = ResponseEntity.status(200).body(null);
        roleType.clear();
        when(staffRefDataServiceMock.getJobTitles())
                .thenReturn(roleType);

        ResponseEntity<?> actual = staffRefDataController.retrieveJobTitles();
        final StaffRefJobTitleResponse actualResponse = (StaffRefJobTitleResponse) actual.getBody();
        assertNotNull(actual);
        verify(staffRefDataServiceMock, times(1))
                .getJobTitles();
        assertEquals(responseEntity.getStatusCode(), actual.getStatusCode());
        assertEquals(0, actualResponse.getJobTitles().size());
        Assertions.assertArrayEquals(actualResponse.getJobTitles().toArray(), roleType.toArray());
    }

    private boolean verifyAllRoleTypes(List<StaffRefDataJobTitle> actualResultUserType, List<RoleType> roleTypes) {
        for (int i = 0; i < actualResultUserType.size(); i++) {
            StaffRefDataJobTitle staffRefDataJobTitle = actualResultUserType.get(i);
            Optional<RoleType> roleType = roleTypes.stream().filter(e ->
                    e.getRoleId().equals(staffRefDataJobTitle.getRoleId())
                            && e.getDescription().equals(staffRefDataJobTitle.getRoleDescription())).findAny();
            if (!roleType.isPresent()) {
                return false;
            }
        }
        return true;
    }
}
