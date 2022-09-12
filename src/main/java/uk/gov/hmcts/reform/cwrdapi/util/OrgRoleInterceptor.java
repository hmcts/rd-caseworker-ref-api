package uk.gov.hmcts.reform.cwrdapi.util;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.AmRoleAssignmentFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.RoleAssignmentQueryResponse;
import uk.gov.hmcts.reform.cwrdapi.repository.IdamRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;

@Component
public class OrgRoleInterceptor implements HandlerInterceptor {
    @Autowired
    @Lazy
    AmRoleAssignmentFeignClient amRoleAssignmentFeignClient;

    @Autowired
    @Lazy
    IdamRepository idamRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object handler) {

        UserInfo userInfo = idamRepository.getUserInfo(getUserToken());

        if (null != userInfo && StringUtils.isNotEmpty(userInfo.getUid())) {
            RoleAssignmentQueryRequest roleAssignmentQueryRequest = RoleAssignmentQueryRequest
                    .roleAssignmentQueryRequest()
                    .roleName(Collections.singletonList("staff-admin"))
                    .actorId(Collections.singletonList(userInfo.getUid()))
                    .build();
            var queryResponse =
                    amRoleAssignmentFeignClient.fetchRoles(roleAssignmentQueryRequest);
            if (queryResponse.status() != HttpStatus.OK.value()) {
                throw new StaffReferenceException(HttpStatus.valueOf(queryResponse.status()),
                        "The user does not have staff-admin role", "The user does not have staff-admin role");
            }
            ResponseEntity<Object> responseEntity = toResponseEntity(queryResponse,
                    RoleAssignmentQueryResponse.class);

            RoleAssignmentQueryResponse roleAssignmentQueryResponse =
                    (RoleAssignmentQueryResponse) responseEntity.getBody();
            if (roleAssignmentQueryResponse.getRoleAssignmentResponse().isEmpty() ) {
                throw new StaffReferenceException(HttpStatus.FORBIDDEN, "The user does not have staff-admin role",
                        "The user does not have staff-admin role");
            }
        }

        return true;
    }
    public String getUserToken() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getTokenValue();
    }
}
