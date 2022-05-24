package uk.gov.hmcts.reform.cwrdapi.config;

import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.UnauthorizedException;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Configuration
public class SecurityEndpointFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION2 = "ServiceAuthorization";

    @Autowired
    IdamApi idamApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (StringUtils.isNotEmpty(request.getRequestURI()) && request.getRequestURI().contains("/refdata")) {
                logger.debug("The Service Auth token length is: "
                        + request.getHeader(SERVICE_AUTHORIZATION2).length());
                logger.debug("The User Auth token length : "
                        + request.getHeader(AUTHORIZATION).length());
                logger.debug("The User Auth token contains 'Bearer '? : "
                        + request.getHeader(AUTHORIZATION).contains("Bearer "));
                if (logger.isDebugEnabled()) {
                    UserInfo userInfo = idamApi.retrieveUserInfo(request.getHeader(AUTHORIZATION));
                    logger.debug(userInfo);
                }
            }
        } catch (Exception e) {
            logger.info("Exception while processing the user or service tokens : " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof FeignException.FeignClientException feignClientException) {
                response.setStatus(feignClientException.status());
                return;
            } else if (e instanceof UnauthorizedException) {
                logger.error("Authorisation exception", e);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                return;
            }
            throw e;
        }
    }
}