package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.generateToken;

@Slf4j
@PropertySource(value = "/integrationTest/resources/application-test.yml")
public class CaseWorkerReferenceDataClient {

    private static final String APP_BASE_PATH = "/refdata/case-worker";
    private static final String APP_INTERNAL_BASE_PATH = "/refdata/internal/staff";

    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    public static final String STAFF_EMAIL_TEMPLATE = "staff-profile-func-test-user-%s@justice.gov.uk";
    private static String JWT_TOKEN = null;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    private String baseUrl;
    private String baseInternalUrl;
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
        this.baseInternalUrl = "http://localhost:" + port + APP_INTERNAL_BASE_PATH;
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

    public Object retrieveAllServiceSkills(Class<?> clazz,
                                           String path, String role) throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = getRequest(path, clazz, role);
        return mapServiceSkillsIdResponse(responseEntity, clazz);
    }

    private Object mapServiceSkillsIdResponse(ResponseEntity<Object> responseEntity,
                                              Class<?> clazz) throws JsonProcessingException {
        HttpStatus status = responseEntity.getStatusCode();

        if (status.is2xxSuccessful()) {
            return objectMapper.convertValue(responseEntity.getBody(), clazz);
        } else {
            Map<String, Object> errorResponseMap = new HashMap<>();
            errorResponseMap.put(
                    "response_body",
                    objectMapper.readValue(responseEntity.getBody().toString(), clazz)
            );
            errorResponseMap.put("http_status", status);
            return errorResponseMap;
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ResponseEntity<Object> getRequest(String uriPath, Class clasz, String role) {

        ResponseEntity<Object> responseEntity;
        try {
            HttpEntity<?> request = new HttpEntity<>(getMultipleAuthHeadersWithoutContentType(role, null));

            responseEntity = restTemplate.exchange(
                    baseUrl + uriPath,
                    HttpMethod.GET,
                    request,
                    clasz
            );
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getResponseBodyAsString());
        }
        return responseEntity;
    }



    public Map<String, Object> fetchStaffProfileByCcdServiceName(String ccdServiceNames, Integer pageSize,
                                                                 Integer pageNumber, String sortDirection,
                                                                 String sortColumn, String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/usersByServiceName");
        if (StringUtils.isNotBlank(ccdServiceNames)) {
            stringBuilder.append("?ccd_service_names=");
            stringBuilder.append(ccdServiceNames);
        }
        if (pageSize != null) {
            stringBuilder.append("&page_size=");
            stringBuilder.append(pageSize);
        }
        if (pageNumber != null) {
            stringBuilder.append("&page_number=");
            stringBuilder.append(pageNumber);
        }
        if (StringUtils.isNotBlank(sortDirection)) {
            stringBuilder.append("&sort_direction=");
            stringBuilder.append(sortDirection);
        }
        if (StringUtils.isNotBlank(sortColumn)) {
            stringBuilder.append("&sort_column=");
            stringBuilder.append(sortColumn);
        }

        ResponseEntity<Map> responseEntity;
        HttpEntity<String> request =
                new HttpEntity<>(getMultipleAuthHeadersWithoutContentType(role, null));


        try {

            responseEntity = restTemplate.exchange(
                    baseInternalUrl  + stringBuilder.toString(),
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

    public Map<String, Object> searchStaffUserByName(String path,String searchString, String pageSize,

                                                                 String pageNumber,  String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(path);

        if (StringUtils.isNotBlank(searchString)) {
            stringBuilder.append("?search=");
            stringBuilder.append(searchString);
        }

        HttpHeaders headers =  getMultipleAuthHeadersWithPagination(role, null,pageNumber,pageSize);

        ResponseEntity<Map> responseEntity;
        HttpEntity<String> request =
                new HttpEntity<>(headers);


        try {

            responseEntity = restTemplate.exchange(
                    baseUrl  + stringBuilder.toString(),
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

    public ResponseEntity<SearchStaffUserResponse[]> searchStaffUserByNameExchange(
            String path,String searchString, String pageSize, String pageNumber,  String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(path);

        if (StringUtils.isNotBlank(searchString)) {
            stringBuilder.append("?search=");
            stringBuilder.append(searchString);
        }

        HttpHeaders headers =  getMultipleAuthHeadersWithPagination(role, null,pageNumber,pageSize);

        ResponseEntity<SearchStaffUserResponse[]> responseEntity = null;
        HttpEntity<String> request =
                new HttpEntity<>(headers);

        try {

            responseEntity = restTemplate.exchange(
                    baseUrl  + stringBuilder.toString(),
                    HttpMethod.GET, request,
                    SearchStaffUserResponse[].class
            );

        } catch (RestClientResponseException ex) {
            log.error(ex.getResponseBodyAsString());
        }

        return responseEntity;
    }

    public ResponseEntity<List<SearchStaffUserResponse>> searchStaffUserExchange(
            String path,String searchString, String pageSize, String pageNumber,  String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(path);

        if (StringUtils.isNotBlank(searchString)) {
            stringBuilder.append(searchString);
        }

        HttpHeaders headers =  getMultipleAuthHeadersWithPagination(role, null,pageNumber,pageSize);

        ResponseEntity<List<SearchStaffUserResponse>> responseEntity = null;
        HttpEntity<String> request =
                new HttpEntity<>(headers);

        try {

            responseEntity = restTemplate.exchange(
                    baseUrl + stringBuilder.toString(),
                    HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<SearchStaffUserResponse>>() {
                    }
            );

        } catch (RestClientResponseException ex) {
            log.error(ex.getResponseBodyAsString());
        }

        return responseEntity;
    }

    public Map<String, Object> searchStaffUser(
            String path,String searchString, String pageSize, String pageNumber,  String role) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(path);

        if (StringUtils.isNotBlank(searchString)) {
            stringBuilder.append(searchString);
        }

        HttpHeaders headers =  getMultipleAuthHeadersWithPagination(role, null,pageNumber,pageSize);

        ResponseEntity<Map> responseEntity;
        HttpEntity<String> request =
                new HttpEntity<>(headers);

        try {

            responseEntity = restTemplate.exchange(
                    baseUrl  + stringBuilder.toString(),
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

    private <T> Map<String, Object> putRequest(String uriPath, T requestBody, String role, String userId) {

        HttpEntity<T> request = new HttpEntity<>(requestBody, getMultipleAuthHeaders(role, userId));

        ResponseEntity<Map> responseEntity;

        try {
            responseEntity = restTemplate.exchange(uriPath, HttpMethod.PUT, request, Map.class);

        } catch (RestClientResponseException ex) {
            HashMap<String, Object> statusAndBody = new HashMap<>(2);
            statusAndBody.put("http_status", String.valueOf(ex.getRawStatusCode()));
            statusAndBody.put("response_body", ex.getResponseBodyAsString());
            return statusAndBody;
        }

        return getResponse(responseEntity);
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

        HttpHeaders headers = getMultipleAuthHeadersWithoutContentType(role, userId);
        headers.setContentType(APPLICATION_JSON);

        return headers;
    }

    private HttpHeaders getMultipleAuthHeaders(String role) {

        return getMultipleAuthHeaders(role, null);
    }

    private HttpHeaders getMultipleAuthHeadersWithPagination(
            String role,
            String userId,
            String pageNumber,
            String pageSize) {

        HttpHeaders headers = getMultipleAuthHeadersWithoutContentType(role, userId);
        headers.setContentType(APPLICATION_JSON);
        if (StringUtils.isNotBlank(pageNumber)) {
            headers.add("page-number", pageNumber);
        }
        if (StringUtils.isNotBlank(pageSize)) {
            headers.add("page-size", pageSize);
        }


        return headers;
    }




    @NotNull
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

    private Object getNewResponse(ResponseEntity<Map> responseEntity) {

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

    public Map<String, Object> createStaffProfile(StaffProfileCreationRequest request, String role) {
        return postRequest(baseUrl + "/profile", request, role, null);
    }


    public StaffProfileCreationRequest createStaffProfileCreationRequest() {

        String emailPattern = "deleteTest1234";
        String email = format(STAFF_EMAIL_TEMPLATE, RandomStringUtils.randomAlphanumeric(10)
                + emailPattern).toLowerCase();

        List<StaffProfileRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(StaffProfileRoleRequest.staffProfileRoleRequest()
                        .roleId(2)
                        .role("Legal Caseworker")
                        .isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(12345)
                .location("test location").build(),CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(6789)
                .location("test location2").build());

        List<CaseWorkerServicesRequest> caseWorkerServicesRequests = ImmutableList.of(CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .service("Immigration and Asylum Appeals").serviceCode("serviceCode2")
                .build(),CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .service("Divorce").serviceCode("ABA1")
                .build());

        List<SkillsRequest> skillsRequest = ImmutableList.of(SkillsRequest
                .skillsRequest()
                .skillId(9)
                .skillCode("1")
                .description("testskill1")
                .build());

        return   StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .firstName("StaffProfilefirstName")
                .lastName("StaffProfilelastName")
                .emailId(email)
                .regionId(1).userType("CTSC")
                .region("National")
                .suspended(false)
                .taskSupervisor(true)
                .caseAllocator(true)
                .staffAdmin(false)
                .roles(caseWorkerRoleRequests)
                .baseLocations(caseWorkerLocationRequests)
                .services(caseWorkerServicesRequests)
                .skills(skillsRequest)
                .build();
    }

    public Map<String, Object> updateStaffProfile(StaffProfileCreationRequest request, String role) {
        return putRequest(baseUrl + "/profile", request, role, null);
    }
}
