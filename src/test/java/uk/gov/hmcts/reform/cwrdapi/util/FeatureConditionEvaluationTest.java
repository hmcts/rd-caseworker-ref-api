package uk.gov.hmcts.reform.cwrdapi.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import uk.gov.hmcts.reform.cwrdapi.controllers.WelcomeController;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ForbiddenException;
import uk.gov.hmcts.reform.cwrdapi.service.impl.FeatureToggleServiceImpl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.assertThrows;
=======
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
>>>>>>> RDCC-3618
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeatureConditionEvaluationTest {

    FeatureToggleServiceImpl featureToggleService = mock(FeatureToggleServiceImpl.class);
    @Spy
    FeatureConditionEvaluation featureConditionEvaluation = new FeatureConditionEvaluation(featureToggleService);
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    Method method = mock(Method.class);


    @BeforeEach
    void before() {
        MockitoAnnotations.openMocks(this);
        when(method.getName()).thenReturn("test");
        doReturn(WelcomeController.class).when(method).getDeclaringClass();
        when(handlerMethod.getMethod()).thenReturn(method);
    }

    @Test
    void testPreHandleValidFlag() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        String token = generateDummyS2SToken("rd_caseworker_ref_api");
        when(httpRequest.getHeader(FeatureConditionEvaluation.SERVICE_AUTHORIZATION))
                .thenReturn(FeatureConditionEvaluation.BEARER + token);
        when(featureToggleService.isFlagEnabled(anyString(),anyString())).thenReturn(true);
        assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleInvalidFlag() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        String token = generateDummyS2SToken("rd_caseworker_ref_api");
        when(httpRequest.getHeader(FeatureConditionEvaluation.SERVICE_AUTHORIZATION))
                .thenReturn(FeatureConditionEvaluation.BEARER + token);
        when(featureToggleService.isFlagEnabled(anyString(),anyString())).thenReturn(false);
        assertThrows(ForbiddenException.class,() -> featureConditionEvaluation.preHandle(httpRequest,
                httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleInvalidServletRequestAttributes() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        assertThrows(ForbiddenException.class,() -> featureConditionEvaluation.preHandle(httpRequest,
                httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleNoFlag() throws Exception {
<<<<<<< HEAD
        assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
=======
        boolean response = featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod);
        assertThat(response).isTrue();
>>>>>>> RDCC-3618
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

    @Test
    void testPreHandleNonConfiguredValues() throws Exception {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("DummyController.test", "test-flag");
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        assertTrue(featureConditionEvaluation.preHandle(httpRequest, httpServletResponse, handlerMethod));
        verify(featureConditionEvaluation, times(1))
                .preHandle(httpRequest, httpServletResponse, handlerMethod);
    }

<<<<<<< HEAD
=======
    @Test
    void testGetServiceName() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("WelcomeController.test", "test-flag");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        String token = generateDummyS2SToken("rd_caseworker_ref_api");
        when(httpRequest.getHeader(FeatureConditionEvaluation.SERVICE_AUTHORIZATION))
                .thenReturn(token);
        when(featureToggleService.isFlagEnabled(anyString(),anyString())).thenReturn(true);

        String serviceName = featureConditionEvaluation.getServiceName("test-flag");
        assertThat(serviceName).isNotEmpty().isEqualTo("rd_caseworker_ref_api");
    }

>>>>>>> RDCC-3618
    static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }
}
