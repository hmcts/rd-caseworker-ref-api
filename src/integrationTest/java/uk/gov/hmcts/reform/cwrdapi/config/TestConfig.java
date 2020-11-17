package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationService;

@Configuration
public class TestConfig {

    @Bean
    ValidationService validationService() {
        return new ValidationService();
    }

    @Bean
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer() {
        return new JsrValidatorInitializer();
    }

    @Bean
    AuditService auditService() {
        return new AuditService();
    }
}
