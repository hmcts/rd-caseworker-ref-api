package uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.CaseWorkerInternalApiClient;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SERVICE_AUTHORIZATION;

@Service
public class CaseWorkerInternalApiClientImpl implements CaseWorkerInternalApiClient {


    @Autowired
    RestTemplate restTemplate;
    @Value("${server.port}")
    private int port;

    @Override
    public <T> ResponseEntity<Object> postRequest(List<T> requestBody,
                                                  String path) {
        UriComponents uriComponents = UriComponentsBuilder
            .newInstance()
            .scheme("http")
            .host("localhost")
            .port(port)
            .path("refdata/case-worker")
            .path(path)
            .build();
        HttpHeaders httpHeaders = getMultipleAuthHeaders();
        HttpEntity<List<T>> request =
            new HttpEntity<>(requestBody, httpHeaders);

        return restTemplate.postForEntity(
            uriComponents.toUri(),
            request,
            Object.class);
    }

    private HttpHeaders getMultipleAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ServletRequestAttributes servletRequestAttributes =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (nonNull(servletRequestAttributes)) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            headers.add(SERVICE_AUTHORIZATION, request.getHeader(SERVICE_AUTHORIZATION));
            String bearerToken = request.getHeader(AUTHORIZATION);
            headers.add(AUTHORIZATION, bearerToken);
        }
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
