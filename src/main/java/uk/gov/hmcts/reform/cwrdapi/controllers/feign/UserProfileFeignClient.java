package uk.gov.hmcts.reform.cwrdapi.controllers.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.cwrdapi.config.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.UserProfileUpdatedData;

@FeignClient(name = "UserProfileClient", url = "${userProfUrl}", configuration = FeignInterceptorConfiguration.class)
public interface UserProfileFeignClient {

    @PostMapping(value = "/v1/userprofile")
    @RequestLine("POST /v1/userprofile")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response createUserProfile(@RequestBody UserProfileCreationRequest userProfileCreationRequest);

    @PutMapping(value = "/v1/userprofile/{userId}")
    @RequestLine("PUT /v1/userprofile/{userId}")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response modifyUserRoles(@RequestBody UserProfileUpdatedData modifyRoles, @PathVariable("userId") String userId,
                             @RequestParam(value = "origin") String origin);

    @GetMapping(value = "/v1/userprofile/{id}/roles")
    @RequestLine("GET /v1/userprofile/{id}/roles")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
        "Content-Type: application/json"})
    Response getUserProfileWithRolesById(@PathVariable String id);

    @DeleteMapping(value = "/v1/userprofile/users?userId={userId}")
    @RequestLine("DELETE /v1/userprofile/users?userId={userId}")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
        "Content-Type: application/json"})
    Response deleteUserProfileById(@PathVariable String userId);

    @DeleteMapping(value = "/v1/userprofile/users?emailPattern={emailPattern}")
    @RequestLine("DELETE /v1/userprofile/users?emailPattern={emailPattern}")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
        "Content-Type: application/json"})
    Response deleteUserProfileByEmailPattern(@PathVariable String emailPattern);

}
