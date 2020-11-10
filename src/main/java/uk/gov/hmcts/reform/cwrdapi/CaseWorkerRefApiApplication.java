package uk.gov.hmcts.reform.cwrdapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.cwrdapi" }, basePackageClasses = { IdamApi.class }
)
@SuppressWarnings("HideUtilityClassConstructor")
public class CaseWorkerRefApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseWorkerRefApiApplication.class, args);
    }
}
