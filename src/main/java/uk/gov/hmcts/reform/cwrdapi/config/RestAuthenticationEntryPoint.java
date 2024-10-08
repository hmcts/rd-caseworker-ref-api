package uk.gov.hmcts.reform.cwrdapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component("restAuthenticationEntryPoint")
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse errorResponse = new ErrorResponse(
            HttpServletResponse.SC_UNAUTHORIZED,
            "UNAUTHORIZED",
            authenticationException.getMessage(),
            "Authentication Exception",
            LocalDateTime.now().toString()
        );
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorMessage = mapper.writeValueAsString(errorResponse);
        response.setHeader("UnAuthorized-Token-Error", errorMessage);
        log.error(errorMessage);

    }
}