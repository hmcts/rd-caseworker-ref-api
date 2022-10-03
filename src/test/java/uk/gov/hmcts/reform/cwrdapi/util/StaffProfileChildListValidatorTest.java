package uk.gov.hmcts.reform.cwrdapi.util;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;

import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintValidatorContext;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LOCATION_FIELD;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@Disabled
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

        CaseWorkerRoleRequest caseWorkerRoleRequest =
                new CaseWorkerRoleRequest("testRole", true);
        CaseWorkerRoleRequest caseWorkerRoleRequest2 =
                new CaseWorkerRoleRequest("adminRole", true);

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
                .skillId("skill")
                .description("training")
                .build();

        SkillsRequest skillsRequest2 = SkillsRequest
                .skillsRequest()
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
                .roles(singletonList(caseWorkerRoleRequest))
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest2))
                .baseLocations(List.of(caseWorkerLocationRequest, caseWorkerLocationRequest2))
                .roles(List.of(caseWorkerRoleRequest,caseWorkerRoleRequest2))
                .skills(List.of(skillsRequest,skillsRequest2))
                .build();

        context = mock(ConstraintValidatorContext.class);
        //contextImpl = mock(ConstraintValidatorContextImpl.class);

        staffProfileCreationReq = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .baseLocations(Collections.EMPTY_LIST)
                .roles(List.of(caseWorkerRoleRequest, caseWorkerRoleRequest))
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

    @Test
    void testInValidLocation() {

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(LOCATION_FIELD)).thenReturn(nodeBuilder);

        boolean response = sut.isValidLocations(staffProfileCreationReq, context);
        assertThat(response).isFalse();
    }

    @Test
    void testInValidRoles() {

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

        boolean response = sut.isValidRoles(staffProfileCreationReq, context);
        assertThat(response).isFalse();
    }

    @Test
    void testInValidServiceCode() {

        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        when(builder.addPropertyNode(any())).thenReturn(nodeBuilder);

        boolean response = sut.isValidAreaOfWk(staffProfileCreationReq, context);
        assertThat(response).isFalse();
    }
}