package uk.gov.hmcts.reform.cwrdapi.config;

import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.UnauthorizedException;

import java.io.IOException;

@Configuration
@Slf4j
public class SecurityEndpointFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof FeignException.FeignClientException feignClientException) {
                response.setStatus(feignClientException.status());
                return;
            } else if (e instanceof UnauthorizedException) {
                log.error("Authorisation exception", e);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                return;
            }
            throw e;
        }
    }
}