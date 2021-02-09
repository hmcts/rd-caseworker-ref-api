package uk.gov.hmcts.reform.cwrdapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
@EnableCaching
@EnableJms
@EnableTransactionManagement
@EnableRetry
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.cwrdapi" },
    basePackageClasses = { IdamApi.class }
)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseWorkerRefApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseWorkerRefApiApplication.class, args);
    }
}
