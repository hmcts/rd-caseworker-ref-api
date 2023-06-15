package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import uk.gov.hmcts.reform.cwrdapi.config.EmailDomainPropertyInitiator;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;

class JsrValidateCaseWorkerUpdateRequest {


    @Spy
    @InjectMocks
    JsrValidatorStaffProfile jsrValidatorStaffProfile;

    StaffAuditRepository staffAuditRepository = mock(StaffAuditRepository.class);

    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = mock(JwtGrantedAuthoritiesConverter.class);

    @BeforeEach
    void init() {
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,DWP.GOV.UK,hmrc.gov.uk";
        openMocks(this);
        jsrValidatorStaffProfile.initializeFactory();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc.com", "test.gov", "user@gmail.com"})
    void testValidateStaffProfileEmail(String email) {
        CaseWorkersProfileUpdationRequest profile = buildCaseWorkersProfileUpdationRequest();
        profile.setEmailId(email);
        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateCaseWorkerUpdateRequest(profile, STAFF_PROFILE_CREATE));
        assertThat(exception.getMessage()).contains(INVALID_EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123.name", "name&", "vilas_shelke", "vilas.:\"_shelke", "*()"})
    @DisplayName("staff profile invalid first name")
    void testValidateStaffProfileInvalidFirstName(String name) {
        CaseWorkersProfileUpdationRequest profile = buildCaseWorkersProfileUpdationRequest();
        profile.setFirstName(name);
        InvalidRequestException exception = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateCaseWorkerUpdateRequest(profile, STAFF_PROFILE_CREATE));
        assertThat(exception.getMessage()).contains(FIRST_NAME_INVALID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123.name", "name&", "vilas_shelke", "vilas.:\"_shelke", "*()"})
    @DisplayName("staff profile invalid last name")
    void testValidateStaffProfileInvalidLastName(String name) {
        CaseWorkersProfileUpdationRequest profile = buildCaseWorkersProfileUpdationRequest();
        profile.setLastName(name);

        InvalidRequestException lastNameException = Assertions.assertThrows(InvalidRequestException.class, () ->
                jsrValidatorStaffProfile.validateCaseWorkerUpdateRequest(profile, STAFF_PROFILE_CREATE));
        Assertions.assertNotNull(lastNameException.getLocalizedMessage());
        assertThat(lastNameException.getMessage()).contains(LAST_NAME_INVALID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123name", "IIIIVVI", "vilas-shelke", "Nando's", "Æâçdëøœoo", "Æmaze"})
    @DisplayName("staff profile valid first name")
    void testValidateStaffProfileValidFirstName(String name) {
        CaseWorkersProfileUpdationRequest profile = buildCaseWorkersProfileUpdationRequest();
        profile.setFirstName(name);
        jsrValidatorStaffProfile.validateCaseWorkerUpdateRequest(profile, STAFF_PROFILE_CREATE);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123name", "IIIIVVI", "vilas-shelke", "Nando's", "Æâçdëøœoo", "Æmaze"})
    @DisplayName("staff profile valid last name")
    void testValidateStaffProfileValidLastName(String name) {
        CaseWorkersProfileUpdationRequest profile = buildCaseWorkersProfileUpdationRequest();
        profile.setLastName(name);
        jsrValidatorStaffProfile.validateCaseWorkerUpdateRequest(profile, STAFF_PROFILE_CREATE);
        verify(staffAuditRepository, times(0)).save(any());
    }

    CaseWorkersProfileUpdationRequest buildCaseWorkersProfileUpdationRequest() {

        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName("firstname")
            .lastName("lastname")
            .userId("d1f888d5-8819-41d7-922e-2699444e1a47")
            .emailId("cwr-func-test-user-7u7ph1em1k@justice.gov.uk")
            .suspended(false)
            .build();
        return caseWorkersProfileUpdationRequest;
    }
}
