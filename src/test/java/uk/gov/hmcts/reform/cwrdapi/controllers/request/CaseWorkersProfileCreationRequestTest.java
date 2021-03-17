package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class CaseWorkersProfileCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    Set<String> idamRoles = new HashSet(Arrays.asList("caseworker"));

    @Test
    public void testUserProfileCreationRequestWithConstructor() {

        CaseWorkersProfileCreationRequest request1 = new CaseWorkersProfileCreationRequest(
                "firstName","lastName", "some@email.com",
                1, UserTypeRequest.INTERNAL.name(), "region", false,
                idamRoles, null, null, null, 1L);

        verify(request1);
    }

    @Test
    public void testUserProfileCreationRequestWithNameLongerThan150CharactersIsConstraintViolation() {
        CaseWorkersProfileCreationRequest request1 = new CaseWorkersProfileCreationRequest(
                RandomStringUtils.randomAlphabetic(151),"lastName",
                "some@email.com", 1, UserTypeRequest.INTERNAL.name(), "region", false,
                idamRoles, null, null, null, 1L);

        Set<ConstraintViolation<CaseWorkersProfileCreationRequest>> violations = validator
                .validate(request1);

        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void testUserProfileCreationRequestWithNameIncludingUnallowedSpecialCharactersIsConstraintViolation() {
        CaseWorkersProfileCreationRequest request1 = new CaseWorkersProfileCreationRequest(
                RandomStringUtils.randomAlphabetic(10) + "*","lastName",
                "some@email.com", 1, UserTypeRequest.INTERNAL.name(), "region", false,
                idamRoles, null, null, null, 1L);

        Set<ConstraintViolation<CaseWorkersProfileCreationRequest>> violations = validator
                .validate(request1);

        assertThat(violations.size()).isEqualTo(1);
    }

    public void verify(CaseWorkersProfileCreationRequest request) {
        assertThat(request.getEmailId()).isEqualTo("some@email.com");
        assertThat(request.getFirstName()).isEqualTo("firstName");
        assertThat(request.getLastName()).isEqualTo("lastName");
        assertThat(request.getRegionId()).isEqualTo(1);
        assertThat(request.getUserType()).isEqualTo(UserTypeRequest.INTERNAL.name());
        assertThat(request.getRegion()).isEqualTo("region");
        assertThat(request.getIdamRoles()).isEqualTo(idamRoles);
    }
}
