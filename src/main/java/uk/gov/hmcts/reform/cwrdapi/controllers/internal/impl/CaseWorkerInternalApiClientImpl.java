package uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl;

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
import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class CaseWorkerInternalApiClientImpl implements CaseWorkerInternalApiClient {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
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
