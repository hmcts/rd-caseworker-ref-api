package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CasWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.service.AuditService;
import uk.gov.hmcts.reform.cwrdapi.service.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.ValidationService;

@Configuration
public class TestConfig {

    @Bean
    ValidationService validationService() {
        return new ValidationService();
    }

    @Bean
    JsrValidatorInitializer<CasWorkerDomain> jsrValidatorInitializer() {
        return new JsrValidatorInitializer();
    }

    @Bean
    AuditService auditService() {
        return new AuditService();
    }
}
