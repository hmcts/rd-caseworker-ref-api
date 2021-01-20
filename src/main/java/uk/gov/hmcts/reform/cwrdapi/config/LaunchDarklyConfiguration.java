package uk.gov.hmcts.reform.cwrdapi.config;

import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;

@Configuration
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return new LDClient(sdkKey);
    }

    @Autowired
    private FeatureConditionEvaluation featureConditionEvaluation;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureConditionEvaluation)
                .addPathPatterns("/refdata/case-worker/users/fetchUsersById",
                        "/refdata/case-worker/idam-roles-mapping",
                        "/refdata/case-worker/users",
                        "/refdata/case-worker/upload-file");

    }
}
