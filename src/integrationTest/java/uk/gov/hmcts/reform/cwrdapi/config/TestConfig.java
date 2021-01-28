package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.IAuditAndExceptionRepositoryService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditAndExceptionRepositoryServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

@Configuration
@Lazy
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

    @Bean
    CaseWorkerReferenceDataClient caseWorkerReferenceDataClient(@Value("${local.server.port}") int port) {
        return new CaseWorkerReferenceDataClient(port);
    }

    @Bean
    CaseWorkerService caseWorkerService() {
        return new CaseWorkerServiceImpl();
    }

}
