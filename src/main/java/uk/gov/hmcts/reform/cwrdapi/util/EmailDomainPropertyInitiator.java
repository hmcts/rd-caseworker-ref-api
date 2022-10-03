package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Slf4j
@NoArgsConstructor
@Setter
@Getter
@Configuration
public class EmailDomainPropertyInitiator {
    public static String emailDomains;
    @Value("${email.domainList}")
    private String tempEmailDomains;

    private static void setStaticEmailList(String emailDomains) {
        EmailDomainPropertyInitiator.emailDomains = emailDomains.toLowerCase(Locale.ENGLISH);
    }

    @Bean
    @PostConstruct
    public void getPropertySupportBean() {
        if (ObjectUtils.isNotEmpty(this.tempEmailDomains)) {
            setStaticEmailList(this.tempEmailDomains.toLowerCase());
        }
    }

}
