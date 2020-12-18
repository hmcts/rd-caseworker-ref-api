package uk.gov.hmcts.reform.cwrdapi.config;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
    @Override
    protected AccessDecisionManager accessDecisionManager() {

        ExpressionBasedPreInvocationAdvice expressionAdvice = new ExpressionBasedPreInvocationAdvice();
        expressionAdvice.setExpressionHandler(getExpressionHandler());
        RoleVoter voter = new RoleVoter();
        voter.setRolePrefix("");
        ImmutableList<AccessDecisionVoter<?>> decisionVoters
                = ImmutableList.of(voter, new AuthenticatedVoter());
        return new AffirmativeBased(decisionVoters);
    }
}
