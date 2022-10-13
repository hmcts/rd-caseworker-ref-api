package uk.gov.hmcts.reform.cwrdapi.util;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;

import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class StaffProfileChildListValidatorTest {

    @InjectMocks
    StaffProfileChildListValidator sut;
    static StaffProfileCreationRequest staffProfileCreationRequest;
    static StaffProfileCreationRequest staffProfileCreationReq;
    static ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContextImpl.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContextImpl.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void reset() {
        Mockito.reset();
    }

    @BeforeAll
    static void setUp() {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1,"testRole", true);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1,"adminRole", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();
        CaseWorkerLocationRequest caseWorkerLocationRequest2 = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("LocationSecond")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest2 = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA5")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .skillId("skill")
                .description("training")
                .build();

        SkillsRequest skillsRequest2 = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .skillId("skill2")
                .description("training2")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest2))
                .baseLocations(List.of(caseWorkerLocationRequest, caseWorkerLocationRequest2))
                .roles(List.of(staffProfileRoleRequest1,staffProfileRoleRequest2))
                .skills(List.of(skillsRequest,skillsRequest2))
                .build();

        context = mock(ConstraintValidatorContext.class);
        //contextImpl = mock(ConstraintValidatorContextImpl.class);

        staffProfileCreationReq = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .baseLocations(Collections.EMPTY_LIST)
                .roles(List.of(staffProfileRoleRequest1, staffProfileRoleRequest2))
                .services(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest))
                .skills(Collections.EMPTY_LIST)
                .build();

    }

    @Test
    void testIsValid() {
        boolean response = sut.isValid(staffProfileCreationRequest, context);
        assertThat(response).isTrue();
    }

    @Test
    void testValidLocation() {
        boolean response = sut.isValidLocations(staffProfileCreationRequest, context);
        assertThat(response).isTrue();
    }

    @Test
    void testValidRoles() {
        boolean response = sut.isValidRoles(staffProfileCreationRequest, context);
        assertThat(response).isTrue();
    }

    @Test
    void isValidAreaOfWk() {

        boolean response = sut.isValidAreaOfWk(staffProfileCreationRequest, context);
        assertThat(response).isTrue();
    }
}