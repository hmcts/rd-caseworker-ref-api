package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.IAuditAndExceptionRepositoryService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditAndExceptionRepositoryServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationService;

@Configuration
public class TestConfig {

    @Bean
    ValidationServiceFacadeImpl validationServiceFacadeImpl() {
        return new ValidationServiceFacadeImpl();
    }

    @Bean
    JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer() {
        return new JsrValidatorInitializer();
    }

    @Bean
    IAuditAndExceptionRepositoryService auditAndExceptionRepositoryService() {
        return new AuditAndExceptionRepositoryServiceImpl();
    }
}
