package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.generateToken;

@Slf4j
@PropertySource(value = "/integrationTest/resources/application-test.yml")
public class CaseWorkerReferenceDataClient {

    private static final String APP_BASE_PATH = "/refdata/case-worker";
    private static String JWT_TOKEN = null;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    private String baseUrl;
    @Value("${oidc.issuer}")
    private String issuer;
    @Value("${oidc.expiration}")
    private long expiration;
    @Autowired
    Environment environment;
    static String bearerToken;
    @Value("${idam.s2s-authorised.services}")
    private String serviceName;

    public CaseWorkerReferenceDataClient(int port) {
        this.baseUrl = "http://localhost:" + port + APP_BASE_PATH;
    }

    public Map<String, Object> createCaseWorkerProfile(CaseWorkersProfileCreationRequest request, String role) {
        return postRequest(baseUrl + "/users/", request, role, null);
    }

    public Map<String, Object> createCaseWorkerProfile(List<CaseWorkersProfileCreationRequest> request, String role) {
        return postRequest(baseUrl + "/users/", request, role, null);
    }

    public Map<String, Object> deleteCaseWorker(String path) {
        return deleteRequest(baseUrl + path, null);
    }

    public Map<String, Object> createIdamRolesAssoc(List<ServiceRoleMapping> serviceRoleMapping, String role) {
        return postRequest(baseUrl + "/idam-roles-mapping/", serviceRoleMapping, role, null);
    }

    public <T> Map<String, Object> uploadCwFile(MultiValueMap<String, Object> body, String role) {
        String uriPath = baseUrl + "/upload-file";
        HttpHeaders httpHeaders = getMultipleAuthHeaders(role);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, httpHeaders);
        return sendRequest(uriPath, request);
    }

    public Map<String, Object> fetchStaffProfileByCcdServiceName(String ccdServiceNames, Integer pageSize,
                                                                 Integer pageNumber, String sortDirection,
                                                                 String sortColumn, String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/get-users-by-service-name");
        if(StringUtils.isNotBlank(ccdServiceNames)) {
            stringBuilder.append("?ccd_service_names=");
            stringBuilder.append(ccdServiceNames);
        }
        if(pageSize != null) {
            stringBuilder.append("&page_size=");
            stringBuilder.append(pageSize);
        }
        if(pageNumber != null) {
            stringBuilder.append("&page_number=");
            stringBuilder.append(pageNumber);
        }
        if(StringUtils.isNotBlank(sortDirection)) {
            stringBuilder.append("&sort_direction=");
            stringBuilder.append(sortDirection);
        }
        if(StringUtils.isNotBlank(sortColumn)) {
            stringBuilder.append("&sort_column=");
            stringBuilder.append(sortColumn);
        }

        ResponseEntity<Map> responseEntity;
        HttpEntity<String> request =
                new HttpEntity<>(getMultipleAuthHeadersWithoutContentType(role, null));


        try {

            responseEntity = restTemplate.exchange(
                    baseUrl + "/users" + stringBuilder.toString(),
                    HttpMethod.GET, request,
                    Map.class
            );

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }


    private <T> Map<String, Object> postRequest(String uriPath, T requestBody, String role, String userId) {

        HttpEntity<T> request = new HttpEntity<>(requestBody, getMultipleAuthHeaders(role, userId));

        return sendRequest(uriPath, request);
    }

    private <T> Map<String, Object> deleteRequest(String uriPath, String role) {

        HttpEntity<T> request = new HttpEntity<>(null, getMultipleAuthHeaders(role, null));

        return sendDeleteRequest(uriPath, request);
    }

    private <T> Map<String, Object> sendRequest(String uriPath, HttpEntity<T> request) {
        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.postForEntity(
                    uriPath,
                    request,
                    Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
    }

    private <T> Map<String, Object> sendDeleteRequest(String uriPath, HttpEntity<T> request) {
        ResponseEntity<Map> responseEntity;

        try {

            responseEntity = restTemplate.exchange(
                    uriPath,
                    DELETE,
                    request,
                    Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getDeleteResponse(responseEntity);
    }

    private HttpHeaders getMultipleAuthHeaders(String role, String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        if (StringUtils.isBlank(JWT_TOKEN)) {

            JWT_TOKEN = generateS2SToken(serviceName);
        }

        headers.add("ServiceAuthorization", JWT_TOKEN);

        if (StringUtils.isBlank(bearerToken)) {
            bearerToken = "Bearer ".concat(getBearerToken(Objects.isNull(userId) ? UUID.randomUUID().toString()
                    : userId, role));
        }
        headers.add("Authorization", bearerToken);

        return headers;
    }

    private HttpHeaders getMultipleAuthHeadersWithoutContentType(String role, String userId) {

        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isBlank(JWT_TOKEN)) {

            JWT_TOKEN = generateS2SToken(serviceName);
        }

        headers.add("ServiceAuthorization", JWT_TOKEN);

        if (StringUtils.isBlank(bearerToken)) {
            bearerToken = "Bearer ".concat(getBearerToken(Objects.isNull(userId) ? UUID.randomUUID().toString()
                    : userId, role));
        }
        headers.add("Authorization", bearerToken);

        return headers;
    }

    private HttpHeaders getMultipleAuthHeaders(String role) {

        return getMultipleAuthHeaders(role, null);
    }

    private Map getResponse(ResponseEntity<Map> responseEntity) {

        Map response = objectMapper
                .convertValue(
                        responseEntity.getBody(),
                        Map.class);

        response.put("http_status", responseEntity.getStatusCode().toString());
        response.put("headers", responseEntity.getHeaders().toString());
        response.put("body", responseEntity.getBody());
        return response;
    }

    private Map getDeleteResponse(ResponseEntity<Map> responseEntity) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", responseEntity.getStatusCode().toString());
        response.put("headers", responseEntity.getHeaders().toString());
        return response;
    }

    private final String getBearerToken(String userId, String role) {
        return generateToken(issuer, expiration, userId, role);
    }

    public static String generateS2SToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }

    public static void setBearerToken(String bearerToken) {
        CaseWorkerReferenceDataClient.bearerToken = bearerToken;
    }
}
