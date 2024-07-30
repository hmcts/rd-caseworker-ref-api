package uk.gov.hmcts.reform.cwrdapi.util;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.cwrdapi.config.EmailDomainPropertyInitiator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailValidatorTest {
    @Mock
    ConstraintValidatorContext context;
    @Mock
    ValidateEmail validateEmail;
    @InjectMocks
    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(ConstraintValidatorContext.class);
        emailValidator = new EmailValidator();
        EmailDomainPropertyInitiator.emailDomains = "justice.gov.uk,dwp.gov.uk,hmrc.gov.uk,hmcts.net,dfcni.gov.uk";
        validateEmail = mock(ValidateEmail.class);
        emailValidator.initialize(validateEmail);
    }

    @Test
    void testInValid_DomainNotSet_Expected_False() {
        //if emailDomainList is not set -> expected false
        var emailId = "vilas.shelke@justice._gov.uk";
        emailValidator.initialize(validateEmail);
        assertThat(emailValidator.isValid(emailId, context)).isFalse();
    }


    @Test
    void testInValid_EmailNotSet_Expected_False() {
        //if email is empty or null it should be false
        assertThat(emailValidator.isValid(null, context)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@justice.gov.uk", "vilas.shelke 123@justice.gov.uk", "vilas.shelke@gmail.com"})
    void testInValid_EmailWithoutFirstName_Expected_False(String emailId) {
        //All emails are not in valid format=> expected false
        assertThat(emailValidator.isValid(emailId, context)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"justice.gov.uk","dwp.gov.uk","hmrc.gov.uk","hmcts.net","dfcni.gov.uk"})
    void testIsValid(String email) {
        assertThat(emailValidator.isValid("test@" + email, context)).isTrue();
    }
}
