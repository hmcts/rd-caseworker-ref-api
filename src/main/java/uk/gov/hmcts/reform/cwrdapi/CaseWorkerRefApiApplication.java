package uk.gov.hmcts.reform.cwrdapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@EnableCaching
@ImportAutoConfiguration({
        FeignAutoConfiguration.class
})
@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.cwrdapi", "uk.gov.hmcts.reform.idam"})
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.cwrdapi" },
        basePackageClasses = { IdamApi.class, ServiceAuthorisationApi.class }
)
@SuppressWarnings({"HideUtilityClassConstructor",
        "checkstyle:Indentation"}) // Spring needs a constructor, its not a utility class
public class CaseWorkerRefApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseWorkerRefApiApplication.class, args);
    }
}
