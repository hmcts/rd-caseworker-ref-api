package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.config.EmailDomainPropertyInitiator;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildStaffProfileRequest;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.MISSING_REGION_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;

class JsrValidatorStaffProfileTest {


    @Spy
    @InjectMocks
    JsrValidatorStaffProfile jsrValidatorStaffProfile;

    StaffProfileAuditServiceImpl staffProfileAuditService = spy(new StaffProfileAuditServiceImpl());

    StaffAudit staffAudit = mock(StaffAudit.class);
    StaffAuditRepository staffAuditRepository = mock(StaffAuditRepository.class);

    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = mock(JwtGrantedAuthoritiesConverter.class);

    @BeforeEach
    void init() {
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,DWP.GOV.UK,hmrc.gov.uk";
        openMocks(this);
        jsrValidatorStaffProfile.initializeFactory();
        setField(staffProfileAuditService, "jwtGrantedAuthoritiesConverter", jwtGrantedAuthoritiesConverter);
        setField(staffProfileAuditService, "staffAuditRepository", staffAuditRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc.com","test.gov","user@gmail.com"})
    void testValidateStaffProfileEmail(String email) {
        StaffProfileCreationRequest profile = buildStaffProfileRequest();
        profile.setEmailId(email);
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE));
        assertThat(exception.getMessage()).contains(INVALID_EMAIL);
    }


    @Test
    @DisplayName("staff profile invalid first name")
    void testValidateStaffProfileInvalidFirstName() {
        StaffProfileCreationRequest profile = buildStaffProfileRequest();
        profile.setFirstName("123.name");
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE));
        assertThat(exception.getMessage()).contains(FIRST_NAME_INVALID);
    }

    @Test
    @DisplayName("staff profile invalid last name")
    void testValidateStaffProfileInvalidLastName() {
        StaffProfileCreationRequest profile = buildStaffProfileRequest();
        profile.setLastName("123.name");
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());

        InvalidRequestException lastNameException = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE));
        Assertions.assertNotNull(lastNameException.getLocalizedMessage());
        assertThat(lastNameException.getMessage()).contains(LAST_NAME_INVALID);
    }

    @Test
    @DisplayName("staff profile missing region")
    void testValidateStaffProfileMissingServices() {
        StaffProfileCreationRequest profile = buildStaffProfileRequest();
        profile.setRegion(StringUtils.EMPTY);
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());

        InvalidRequestException lastNameException = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE));
        Assertions.assertNotNull(lastNameException.getLocalizedMessage());
        assertThat(lastNameException.getMessage()).contains(MISSING_REGION_PROFILE);
    }

    @Test
    @DisplayName("staff profile empty request")
    void testValidateStaffProfileEmptyRequest() {
        StaffProfileCreationRequest profile = StaffProfileCreationRequest.staffProfileCreationRequest().build();
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());

        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE));
        Assertions.assertNotNull(exception.getLocalizedMessage());

    }

    @Test
    @DisplayName("staff profile no violations")
    void testValidateStaffProfileNoViolations() {
        StaffProfileCreationRequest profile = buildStaffProfileRequest();
        when(jwtGrantedAuthoritiesConverter.getUserInfo()).thenReturn(UserInfo.builder().name("test").build());
        when(staffAuditRepository.save(any())).thenReturn(staffAudit.builder().id(1L).build());
        jsrValidatorStaffProfile.validateStaffProfile(profile,STAFF_PROFILE_CREATE);
        verify(staffAuditRepository, times(0)).save(any());
    }
}
