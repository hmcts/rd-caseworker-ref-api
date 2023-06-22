package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceResponseTest {

    @Test
    void testSkillResposne_for_tostring_builder() {

        ServiceResponse serviceResponse = ServiceResponse.builder()
                .service("testservice")
                .serviceCode("testServiceCode")
                .build();

        assertNotNull(serviceResponse);
        assertEquals("testservice",serviceResponse.getService());
        assertEquals("testServiceCode",serviceResponse.getServiceCode());


        String description = ServiceResponse.builder()
                .serviceCode("testServiceCode").toString();


        assertTrue(description
                .contains("ServiceResponse.ServiceResponseBuilder(service=null, serviceCode=testServiceCode)"));

    }
}
