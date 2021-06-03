package uk.gov.hmcts.reform.cwrdapi.controllers.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.cwrdapi.config.FeignInterceptorConfiguration;

@FeignClient(name = "LocationReferenceDataFeignClient", url = "${locationRefDataUrl}",
        configuration = FeignInterceptorConfiguration.class)
public interface LocationReferenceDataFeignClient {

    @GetMapping(value = "/refdata/location/orgServices")
    @RequestLine("GET /refdata/location/orgServices")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response getLocationRefServiceMapping(@RequestParam String ccdServiceNames);

}
