
package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    void test_should_return_welcome_response() {

        ResponseEntity<String> responseEntity = welcomeController.welcome();
        String expectedMessage = "Message for the Caseworker Ref Data API";

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).contains(expectedMessage);
    }
}

