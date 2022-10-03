package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
@NoArgsConstructor
/*
EmailValidator IS EMAIL VALIDATION. THIS ALSO ALLOWED ONLY DOMAINS MENTIONED IN APPLICATION.YAML FILE
@copyrights : hmcts
 */
public class EmailValidator implements ConstraintValidator<ValidateEmail, String> {

    private String emailDomainList;

    @Override
    public void initialize(ValidateEmail validateEmail) {
        this.emailDomainList = EmailDomainPropertyInitiator.emailDomains;
    }

    @Override
    public boolean isValid(String emailAddress, ConstraintValidatorContext constraintValidatorContext) {
        if (ObjectUtils.isEmpty(emailAddress) || ObjectUtils.isEmpty(this.emailDomainList)) {
            log.error("Email id => '{}' or email.domainList => {} should not empty", emailAddress, emailDomainList);
            return false;
        }
        return this.emailPatternMatches(emailAddress.toLowerCase());
    }

    private boolean emailPatternMatches(String emailAddress) {
        String emailDomainName = this.getDomainName(emailAddress);
        if (ObjectUtils.isNotEmpty(emailDomainName) && this.isDomainValid(emailDomainName)) {
            String regexPattern = CaseWorkerConstants.USER_NAME_PATTERN + "@"
                    + emailDomainName;
            return Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)
                    .matcher(emailAddress)
                    .matches();
        }
        return false;
    }

    private boolean isDomainValid(String emailDomainName) {
        List<String> domainList = List.of(this.emailDomainList.split(","));
        return domainList.contains(emailDomainName);
    }

    private String getDomainName(String emailAddress) {
        String[] split = emailAddress.split("@");
        final Integer two = 2;
        if (ObjectUtils.isNotEmpty(split) && split.length == two) {
            return split[1];
        }
        return null;
    }
}
