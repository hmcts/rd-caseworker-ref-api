package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StaffRefDataJobTitleControllerTest {

    @Mock
    StaffRefDataService staffRefDataServiceMock;

    @InjectMocks
    private StaffRefDataController staffRefDataController;


    @Test
    void shouldFetchJobTitles() {
        //given

        final long roleId1 = RandomUtils.nextLong();
        final long roleId2 = RandomUtils.nextLong();
        final String roleDescription1 = RandomStringUtils.randomAlphanumeric(10);
        final String roleDescription2 = RandomStringUtils.randomAlphanumeric(10);

        final List<RoleType> roleTypes = List.of(
                new RoleType(roleId1, roleDescription1),
                new RoleType(roleId2, roleDescription2));


        final StaffRefDataJobTitle staffRefDataJobTitle1 = StaffRefDataJobTitle.builder()
                .roleId(roleId1)
                .roleDescription(roleDescription1)
                .build();

        final StaffRefDataJobTitle staffRefDataJobTitle2 = StaffRefDataJobTitle.builder()
                .roleId(roleId2)
                .roleDescription(roleDescription2)
                .build();

        final List<StaffRefDataJobTitle> staffRefDataJobTitles = List.of(staffRefDataJobTitle1, staffRefDataJobTitle2);
        final StaffRefJobTitleResponse srJobTitleResponse =
                StaffRefJobTitleResponse
                        .builder()
                        .jobTitles(staffRefDataJobTitles)
                        .build();

        final ResponseEntity<Object> expectedResponseEntity = new ResponseEntity<>(srJobTitleResponse, null, HttpStatus.OK);


        //when
        when(staffRefDataServiceMock.getJobTitles()).thenReturn(roleTypes);


        //then
        ResponseEntity<?> actualReponseEntity = staffRefDataController.retrieveJobTitles();
        assertNotNull(actualReponseEntity);
        assertEquals(expectedResponseEntity.getStatusCode(), actualReponseEntity.getStatusCode());

        final StaffRefJobTitleResponse actualStaffRefJobTitleResponse = (StaffRefJobTitleResponse) actualReponseEntity.getBody();
        assertNotNull(actualStaffRefJobTitleResponse);

        final StaffRefJobTitleResponse expectedResponseEntityBody = (StaffRefJobTitleResponse) expectedResponseEntity.getBody();
        assertNotNull(expectedResponseEntityBody);


        final List<StaffRefDataJobTitle> expectedJobTitles = expectedResponseEntityBody.getJobTitles();
        assertNotNull(actualStaffRefJobTitleResponse);

        List<StaffRefDataJobTitle> actualJobTitles = actualStaffRefJobTitleResponse.getJobTitles();
        assertNotNull(actualJobTitles);

        assertEquals(expectedJobTitles.size(), actualJobTitles.size());
        assertThat(expectedJobTitles).usingRecursiveComparison().isEqualTo(actualJobTitles);
        verify(staffRefDataServiceMock).getJobTitles();
    }


    @Test
    void shouldFetchEmptyJobTitles() {
        //given
        final StaffRefJobTitleResponse srJobTitleResponse = StaffRefJobTitleResponse
                .builder()
                .jobTitles(List.of())
                .build();

        final ResponseEntity<Object> expectedResponseEntity = new ResponseEntity<>(srJobTitleResponse, null, HttpStatus.OK);
        //when
        when(staffRefDataServiceMock.getJobTitles()).thenReturn(List.of());

        //then
        final ResponseEntity<?> actualResponseEntity = staffRefDataController.retrieveJobTitles();
        assertNotNull(actualResponseEntity);
        assertEquals(expectedResponseEntity.getStatusCode(), actualResponseEntity.getStatusCode());

        final StaffRefJobTitleResponse actualResponse = (StaffRefJobTitleResponse) actualResponseEntity.getBody();
        assertNotNull(actualResponse);

        final List<StaffRefDataJobTitle> actualJobTitles = actualResponse.getJobTitles();

        assertNotNull(actualJobTitles);
        assertTrue(actualJobTitles.isEmpty());
        verify(staffRefDataServiceMock).getJobTitles();
    }

}