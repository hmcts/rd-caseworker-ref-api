package uk.gov.hmcts.reform.cwrdapi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;

    }

    public String getServiceAuthorizationHeader() {
        return authTokenGenerator.generate();
    }
}