package uk.gov.hmcts.reform.cwrdapi.config;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationServiceFacadeImpl;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.net.SocketException;
import java.util.UUID;

import static java.util.Objects.isNull;
import static org.mockito.Mockito.mock;

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
    CaseWorkerReferenceDataClient caseWorkerReferenceDataClient(@Value("${local.server.port}") int port) {
        return new CaseWorkerReferenceDataClient(port);
    }

    //Create new UUID for each excel rows
    @Bean
    @Primary
    public CaseWorkerServiceImpl caseWorkerProfile() {
        return new CaseWorkerServiceImpl() {
            @Override
            public void populateCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                                  CaseWorkerProfile caseWorkerProfile, String idamId) {
                if (isNull(idamId)) {
                    idamId = UUID.randomUUID().toString();
                }
                super.populateCaseWorkerProfile(cwrdProfileRequest, caseWorkerProfile, idamId);
            }
        };
    }

    @Bean
    @Primary
    public HttpClient testClient() {
        return HttpClientBuilder.create().setRetryHandler((exception, executionCount, context) -> {
            if (executionCount > 5) {
                return false;
            }
            return exception instanceof org.apache.http.NoHttpResponseException || exception instanceof SocketException;
        }).build();
    }

    @Bean
    public ServiceBusSenderClient getServiceBusSenderClient() {
        return mock(ServiceBusSenderClient.class);
    }

}
