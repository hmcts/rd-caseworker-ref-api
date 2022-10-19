package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_UPDATE;

public class StaffProfileAuditServiceImplTest {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Mock
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Mock
    private StaffAuditRepository staffAuditRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    StaffProfileAuditServiceImpl staffProfileAuditServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSaveStaffAudit() throws JsonProcessingException {
        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();
        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        String userId = (nonNull(userInfo) && nonNull(userInfo.getUid())) ? userInfo.getUid() : null;
        String request = objectMapper.writeValueAsString(staffProfileCreationRequest);


        StaffAudit staffAudit = StaffAudit.builder()
                .status(AuditStatus.FAILURE.getStatus().toUpperCase())
                .requestTimeStamp(LocalDateTime.now())
                .errorDescription(null)
                .authenticatedUserId(userId)
                .caseWorkerId("1234")
                .operationType(STAFF_PROFILE_UPDATE)
                .requestLog(request)
                .build();

        when(staffAuditRepository.save(any())).thenReturn(staffAudit);


        staffProfileAuditServiceImpl.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest, STAFF_PROFILE_UPDATE);

        verify(staffAuditRepository, times(1))
                .save(any());

    }


    private StaffProfileCreationRequest getStaffProfileUpdateRequest() {

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        StaffProfileRoleRequest staffProfileRoleRequest =
                new StaffProfileRoleRequest(1, "testRole1", true);

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
                .skillId(1)
                .description("training")
                .build();


        StaffProfileCreationRequest staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("cwr-func-test-user@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(staffProfileRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();

        return staffProfileCreationRequest;

    }
}
