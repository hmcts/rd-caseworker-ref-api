package uk.gov.hmcts.reform.cwrdapi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.getS2sToken;
import static uk.gov.hmcts.reform.lib.idam.IdamOpenId.adminToken;

@Slf4j
@Service
public class FuncTestRequestHandler {

    @Value("${targetInstance}")
    protected String caseWorkerApiUrl;

    public static final String BEARER = "Bearer ";

    private ObjectMapper objectMapper = new ObjectMapper();

    public void sendPut(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {
        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public <T> T sendGet(HttpStatus httpStatus, String urlPath,  Class<T> clazz, String baseUrl,
                         Map<String, String> additionalHeaders) {
        return sendGet(httpStatus, urlPath, baseUrl, additionalHeaders).as(clazz);
    }

    public Response sendGet(HttpStatus httpStatus, String urlPath, String baseUrl,
                            Map<String, String> additionalHeaders) {

        RequestSpecification requestSpecification =  SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(baseUrl)
                .header("ServiceAuthorization", getS2sToken())
                .header("Authorization", BEARER + adminToken);
        if (!additionalHeaders.isEmpty()) {
            additionalHeaders
                    .forEach(requestSpecification::header);

        }
        Response response = requestSpecification.when()
                .get(urlPath);
        log.info("UP get user response status code: {}", response.getStatusCode());
        return response.then()
                .log().all(true)
                .statusCode(httpStatus.value()).extract().response();
    }

    public  <T> T  sendPost(Object data, HttpStatus expectedStatus, String path,String baseUrl, Class<T> clazz,
                            String token) throws JsonProcessingException {
        return sendPost(objectMapper.writeValueAsString(data),
                expectedStatus,
                baseUrl,path,token).as(clazz);
    }

    public Response sendPost(String request,HttpStatus httpStatus,String baseUrl,String urlPath, String token) {

        return SerenityRest
                .given()
                .headers(getHttpHeaders(token))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post(baseUrl + urlPath)
                .then()
                .log().all(true)
                .statusCode(httpStatus.value()).extract().response();
    }

    private HttpHeaders getHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", getS2sToken());
        headers.add("Authorization", BEARER + token);
        return headers;
    }

}