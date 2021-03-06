package uk.gov.hmcts.reform.cwrdapi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.getS2sToken;
import static uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient.crdAdminToken;

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

    public <T> T sendGet(HttpStatus httpStatus, String urlPath, Class<T> clazz, String baseUrl) {
        return sendGet(httpStatus, urlPath, baseUrl).as(clazz);
    }

    public Response sendGet(HttpStatus httpStatus, String urlPath, String baseUrl) {

        Response response =  SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(baseUrl)
                .header("ServiceAuthorization", getS2sToken())
                .header("Authorization", BEARER + crdAdminToken)
                .when()
                .get(urlPath);
        log.info("UP get user response status code: {}", response.getStatusCode());
        return response.then()
                .log().all(true)
                .statusCode(httpStatus.value()).extract().response();
    }

}