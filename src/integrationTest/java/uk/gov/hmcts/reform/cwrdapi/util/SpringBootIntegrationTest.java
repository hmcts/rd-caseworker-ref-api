package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cwrdapi.CaseWorkerRefApiApplication;
import uk.gov.hmcts.reform.cwrdapi.config.TestApplicationServer;
import uk.gov.hmcts.reform.cwrdapi.wiremock.WireMockContextInitializer;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = CaseWorkerRefApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
public abstract class SpringBootIntegrationTest {

    @Autowired
    protected TestApplicationServer testApplicationServer;

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

}
