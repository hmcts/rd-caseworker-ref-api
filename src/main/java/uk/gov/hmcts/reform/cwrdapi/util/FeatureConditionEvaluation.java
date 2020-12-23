package uk.gov.hmcts.reform.cwrdapi.util;

import com.auth0.jwt.JWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ForbiddenException;
import uk.gov.hmcts.reform.cwrdapi.service.FeatureToggleService;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Component
@AllArgsConstructor
@Slf4j
public class FeatureConditionEvaluation implements HandlerInterceptor {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String BEARER = "Bearer ";

    public static final String FORBIDDEN_EXCEPTION_LD = "feature flag is not released";

    @Autowired
    private final FeatureToggleService featureToggleService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {

        Map<String, String> launchDarklyUrlMap = featureToggleService.getLaunchDarklyMap();

        String restMethod = ((HandlerMethod) handler).getMethod().getName();
        String clazz = ((HandlerMethod) handler).getMethod().getDeclaringClass().getSimpleName();
        boolean flagStatus = Boolean.TRUE;
        log.info("CLass: " + clazz + " method : " + restMethod);
        String flagName = launchDarklyUrlMap.get(clazz + "." + restMethod);

        if (isNotTrue(launchDarklyUrlMap.isEmpty()) && nonNull(flagName)) {
            log.info("FlagName  is : " + flagName);
            log.info("ServiceName  is : " + getServiceName(flagName));
            flagStatus = featureToggleService
                    .isFlagEnabled(getServiceName(flagName), launchDarklyUrlMap.get(clazz + "." + restMethod));
            log.info("flagStatus  is : " + flagStatus);
            if (!flagStatus) {
                throw new ForbiddenException(flagName.concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
            }
        }
        return flagStatus;
    }

    public String getServiceName(String flagName) {

        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (nonNull(servletRequestAttributes)) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return JWT.decode(removeBearerFromToken(request.getHeader(SERVICE_AUTHORIZATION))).getSubject();
        }

        throw new ForbiddenException(flagName.concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
    }

    private String removeBearerFromToken(String token) {
        if (isNotTrue(token.startsWith(BEARER))) {
            return token;
        } else {
            return token.substring(BEARER.length());
        }
    }

}
