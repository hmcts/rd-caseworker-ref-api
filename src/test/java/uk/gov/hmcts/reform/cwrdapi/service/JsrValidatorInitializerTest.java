package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.config.EmailDomainPropertyInitiator;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

class JsrValidatorInitializerTest {

    @Spy
    @InjectMocks
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Spy
    @InjectMocks
    JsrValidatorInitializer<StaffProfileCreationRequest> jsrValidatorInitializerProfile;

    ValidationServiceFacadeImpl validationServiceFacadeImpl = spy(new ValidationServiceFacadeImpl());

    StaffAudit staffAudit = mock(StaffAudit.class);
    StaffAuditRepository staffAuditRepository = mock(StaffAuditRepository.class);

    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = mock(JwtGrantedAuthoritiesConverter.class);

    @BeforeEach
    void init() {
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,DWP.GOV.UK,hmrc.gov.uk";
        openMocks(this);
        jsrValidatorInitializer.initializeFactory();
        jsrValidatorInitializerProfile.initializeFactory();
        setField(validationServiceFacadeImpl, "jwtGrantedAuthoritiesConverter", jwtGrantedAuthoritiesConverter);
        setField(validationServiceFacadeImpl, "staffAuditRepository",
                staffAuditRepository);
    }

    @Test
    void testGetNoInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(0, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    void testGetInvalidJsrRecords() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = CaseWorkerProfile.builder().build();
        profile.setOfficialEmail("abc.com");
        caseWorkerProfiles.add(profile);
        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(1, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);

        Set<ConstraintViolation<CaseWorkerDomain>> constraintViolationSet =
                jsrValidatorInitializer.getConstraintViolations();

        assertThat(constraintViolationSet).isNotEmpty();
    }

    @Test
    void testGetInvalidJsrRecords_withDifferentEmails() {
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerDomain profile1 = buildCaseWorkerProfileData("tEst123-CRD3@JUSTICE.GOV.UK");
        CaseWorkerDomain profile2 = buildCaseWorkerProfileData("$%^&@justice.gov.uk");
        CaseWorkerDomain profile3 = buildCaseWorkerProfileData("user name@justice.gov.uk");
        caseWorkerProfiles.add(profile1);
        caseWorkerProfiles.add(profile2);
        caseWorkerProfiles.add(profile3);

        List<CaseWorkerDomain> records = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        assertEquals(2, records.size());
        verify(jsrValidatorInitializer).getInvalidJsrRecords(caseWorkerProfiles);
    }

    @Test
    void testValidateStaffProfile() {
        StaffProfileCreationRequest profile = StaffProfileCreationRequest.staffProfileCreationRequest().build();
        profile.setEmailId("abc.com");
        profile.setFirstName("tester");
        profile.setLastName("tester");
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());


        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorInitializerProfile.validateStaffProfile(profile));
        Assertions.assertNotNull(exception.getLocalizedMessage());
    }

}
