package uk.gov.hmcts.reform.cwrdapi.controllers.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.cwrdapi.config.FeignInterceptorConfiguration;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.RoleAssignmentQueryResponse;

import java.util.List;

@FeignClient(name = "AmRoleAssignmentFeignClient", url = "${amRoleAssignmentUrl}",
        configuration = FeignInterceptorConfiguration.class)
public interface AmRoleAssignmentFeignClient {
    @PostMapping("am/role-assignments/query")
    @RequestLine("POST am/role-assignments/query")
    @Headers({"Authorization: {authorization}", "ServiceAuthorization: {serviceAuthorization}",
            "Content-Type: application/json"})
    Response fetchRoles(@RequestBody RoleAssignmentQueryRequest roleAssignmentQueryRequest);
}
