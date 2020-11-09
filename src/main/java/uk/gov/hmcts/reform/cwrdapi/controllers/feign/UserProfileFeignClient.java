package uk.gov.hmcts.reform.cwrdapi.controllers.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.cwrdapi.config.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;

@FeignClient(name = "UserProfileClient", url = "${userProfUrl}", configuration = FeignInterceptorConfiguration.class)
public interface UserProfileFeignClient {

    @PostMapping(value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);


}
