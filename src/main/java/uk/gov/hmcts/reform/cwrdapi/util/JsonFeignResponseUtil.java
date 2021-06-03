package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ERROR_IN_PARSING_THE_FEIGN_RESPONSE;

@SuppressWarnings("unchecked")
public class JsonFeignResponseUtil {
    private static final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonFeignResponseUtil() {

    }

    public static Optional<Object> decode(Response response, Object clazz) {
        try {
            return Optional.of(json.readValue(response.body().asReader(Charset.defaultCharset()),
                    (Class<Object>) clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static List<Object> mapObjectToList(Response response, Object clazz) {
        try {
            JavaType type = json.getTypeFactory().constructCollectionType(List.class, (Class<?>) clazz);
            return json.readValue(response.body().asReader(Charset.defaultCharset()), type);
        } catch (Exception e) {
            throw new StaffReferenceException(INTERNAL_SERVER_ERROR,
                    String.format(ERROR_IN_PARSING_THE_FEIGN_RESPONSE, ((Class<?>) clazz).getSimpleName()), e);
        }
    }

    public static ResponseEntity<Object> toResponseEntity(Response response, Object clazz) {
        Optional<Object>  payload = decode(response, clazz);

        return new ResponseEntity<>(
                payload.orElse("unknown"),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static ResponseEntity<Object> toResponseEntityWithListBody(Response response, Object clazz) {
        List<Object> payload = mapObjectToList(response, clazz);

        return new ResponseEntity<>(
                payload,
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e -> {
            if (!(e.getKey().equalsIgnoreCase("request-context") || e.getKey()
                    .equalsIgnoreCase("x-powered-by") || e.getKey()
                    .equalsIgnoreCase("content-length"))) {
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        });


        return responseEntityHeaders;
    }
}
