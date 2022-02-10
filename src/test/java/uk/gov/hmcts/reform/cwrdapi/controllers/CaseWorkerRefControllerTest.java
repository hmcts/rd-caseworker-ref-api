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
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceFacadeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerRefControllerTest {

    @InjectMocks
    private CaseWorkerRefController caseWorkerRefController;

    CaseWorkerService caseWorkerServiceMock;
    @Mock
    MultipartFile multipartFile;
    @Mock
    CaseWorkerServiceFacadeImpl caseWorkerServiceFacade;

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest = new ArrayList<>();
    CaseWorkersProfileCreationRequest cwRequest;
    CaseWorkerProfileCreationResponse cwProfileCreationResponse;
    CaseWorkerProfileCreationResponse cwResponse;
    ResponseEntity<Object> responseEntity;

    @BeforeEach
    void setUp() {
        caseWorkerServiceMock = mock(CaseWorkerService.class);
        cwResponse = CaseWorkerProfileCreationResponse
                .builder()
                .caseWorkerRegistrationResponse("Case Worker Profiles Created.")
                .caseWorkerIds(Collections.emptyList())
                .build();
        responseEntity = new ResponseEntity<>(
                cwResponse,
                null,
                HttpStatus.OK
        );
        Set<String> roles = new HashSet<>();
        roles.add("tribunal_case_worker");

        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = new ArrayList<>();
        CaseWorkerRoleRequest cwRoleRequest = new CaseWorkerRoleRequest("role", true);
        caseWorkerRoleRequests.add(cwRoleRequest);

        List<CaseWorkerLocationRequest> caseWorkeLocationRequests = new ArrayList<>();

        CaseWorkerLocationRequest cwLocRequest = new CaseWorkerLocationRequest(1,"location",
                true);
        caseWorkeLocationRequests.add(cwLocRequest);

        List<CaseWorkerWorkAreaRequest> caseWorkeAreaRequests = new ArrayList<>();
        CaseWorkerWorkAreaRequest cwAreaRequest = new CaseWorkerWorkAreaRequest("areaOfwork",
                "serviceCode");
        caseWorkeAreaRequests.add(cwAreaRequest);

        cwRequest = CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .firstName("firstName")
                .lastName("lastName")
                .emailId("test@gmail.com")
                .regionId(1)
                .userType("userType")
                .region("region")
                .suspended(false)
                .idamRoles(roles)
                .roles(caseWorkerRoleRequests)
                .workerWorkAreaRequests(caseWorkeAreaRequests)
                .baseLocations(caseWorkeLocationRequests)
                .rowId(0)
                .caseAllocator(false)
                .taskSupervisor(false)
                .build();
        caseWorkersProfileCreationRequest.add(cwRequest);
        cwProfileCreationResponse = CaseWorkerProfileCreationResponse
                .builder()
                .caseWorkerRegistrationResponse("")
                .caseWorkerIds(Collections.emptyList())
                .build();
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serviceId("BA11")
                .idamRoles("role1")
                .roleId(1)
                .build();

        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse(200, "testMessage");
        when(caseWorkerServiceMock.buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping)))
                .thenReturn(idamRolesMappingResponse);
        ResponseEntity<Object> actual = caseWorkerRefController
                .buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));

        assertNotNull(actual);
        assertThat(actual.getStatusCodeValue()).isEqualTo(200);
        verify(caseWorkerServiceMock,times(1))
                .buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));
    }

    @Test
    void should_throw_exception_for_invalid_request() {

        Assertions.assertThrows(InvalidRequestException.class, () -> {
            List<ServiceRoleMapping> serviceRoleMappingList = Collections.emptyList();
            caseWorkerRefController
                    .buildIdamRoleMappings(serviceRoleMappingList);
        });
        verify(caseWorkerServiceMock,times(0))
                .buildIdamRoleMappings(anyList());
    }

    @Test
    void test_upload_caseworker_file_success() {
        when(caseWorkerServiceFacade.processFile(multipartFile))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        ResponseEntity<Object> actual = caseWorkerRefController
                .caseWorkerFileUpload(multipartFile);
        assertThat(actual.getStatusCodeValue()).isEqualTo(201);

    }

}
